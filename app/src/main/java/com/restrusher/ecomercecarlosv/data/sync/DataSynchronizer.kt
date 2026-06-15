package com.restrusher.ecomercecarlosv.data.sync

import android.content.Context
import android.util.Log
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.data.sync.impl.ClienteSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.MercadoSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.PedidoSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.ProductoSyncer
import com.restrusher.ecomercecarlosv.di.ApplicationScope
import com.restrusher.ecomercecarlosv.domain.error.AppError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSynchronizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val mercadoSyncer: MercadoSyncer,
    private val clienteSyncer: ClienteSyncer,
    private val productoSyncer: ProductoSyncer,
    private val pedidoSyncer: PedidoSyncer,
    private val errorHandler: GlobalErrorHandler,
    @ApplicationScope private val appScope: CoroutineScope,
) {

    companion object {
        /** Master data (mercados, productos) — changes infrequently. */
        const val THRESHOLD_MASTER_MS   = 2 * 60 * 60 * 1000L  // 2 hours

        /** Business data (clientes, pedidos) — changes more often. */
        const val THRESHOLD_BUSINESS_MS = 30 * 60 * 1000L       // 30 minutes

        private const val SYNC_TIMEOUT_MS = 10_000L
        private const val PREFS_NAME = "sync_staleness"
        private const val TAG = "DataSynchronizer"

        private val ALL_ENTITY_TYPES = listOf(
            EntityType.MERCADO,
            EntityType.CLIENTE,
            EntityType.PRODUCTO,
            EntityType.PEDIDO,
        )
    }

    // Persisted across process kills via SharedPreferences.
    // Loaded on init; updated only on successful sync; cleared on email/password login or
    // connectivity restore. Biometric logins leave prefs intact so cached thresholds survive.
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory map. Used as an optimistic dedup guard within a session (prevents two concurrent
    // coroutines from syncing the same entity). Set BEFORE the network call; restored/removed on
    // failure so the next navigation can retry. Also loaded from prefs on startup.
    private val lastSyncedAt = ConcurrentHashMap<String, Long>()

    private val activeSyncs = AtomicInteger(0)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Restore timestamps persisted from the previous session (biometric login path).
        ALL_ENTITY_TYPES.forEach { type ->
            val saved = prefs.getLong(type, 0L)
            if (saved > 0L) lastSyncedAt[type] = saved
        }
    }

    fun start() {
        // When connectivity is restored, mark everything as stale so the next navigation
        // re-fetches fresh data. Also clear prefs so the stale timestamps don't survive a
        // potential process restart while still in the same login session.
        appScope.launch {
            networkMonitor.isOnlineFlow
                .drop(1)
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline) {
                        Log.d(TAG, "connectivity restored — resetting staleness")
                        resetStaleness()
                    }
                }
        }
    }

    /**
     * Called by repositories from their primary read Flow methods. Non-blocking — launches a
     * background coroutine and returns immediately. No-op if [entityType] was successfully
     * synced within [thresholdMs].
     */
    fun triggerSyncIfStale(entityType: String, thresholdMs: Long) {
        appScope.launch { syncIfStale(entityType, thresholdMs) }
    }

    /**
     * Clears both the in-memory staleness map and the persisted SharedPreferences.
     * Call this after an email/password login so the user always gets fresh data.
     * Do NOT call this after biometric login — the thresholds should survive across restarts
     * for biometric users so they don't have to wait for a full re-download each time.
     */
    fun resetStaleness() {
        lastSyncedAt.clear()
        prefs.edit().clear().apply()
        Log.d(TAG, "staleness reset")
    }

    private suspend fun syncIfStale(entityType: String, thresholdMs: Long) {
        if (!networkMonitor.isOnline) return

        val now = System.currentTimeMillis()
        if (now - (lastSyncedAt[entityType] ?: 0L) < thresholdMs) return

        // Optimistic stamp — any concurrent call for the same entity will see it as "in-progress"
        // and exit early. Restored to the previous value on failure so next navigation can retry.
        val previous = lastSyncedAt[entityType] ?: 0L
        lastSyncedAt[entityType] = now

        val syncer: EntitySyncer = when (entityType) {
            EntityType.MERCADO  -> mercadoSyncer
            EntityType.CLIENTE  -> clienteSyncer
            EntityType.PRODUCTO -> productoSyncer
            EntityType.PEDIDO   -> pedidoSyncer
            else -> { lastSyncedAt[entityType] = previous; return }
        }

        if (activeSyncs.incrementAndGet() == 1) _isSyncing.value = true
        try {
            Log.d(TAG, "syncing $entityType")
            val result = withTimeoutOrNull(SYNC_TIMEOUT_MS) { syncer.sync() }
            when {
                result == null -> {
                    // Timeout — restore previous so next navigation retries; don't persist.
                    lastSyncedAt[entityType] = previous
                    errorHandler.emit(AppError.Sync("Sync timed out for $entityType", null))
                    Log.w(TAG, "sync timeout: $entityType")
                }
                result is SyncResult.Failure -> {
                    // Error — restore previous so next navigation retries; don't persist.
                    lastSyncedAt[entityType] = previous
                    errorHandler.emit(AppError.Sync("Failed to sync $entityType", result.error))
                    Log.w(TAG, "sync failure: $entityType — ${result.error.message}")
                }
                else -> {
                    // Success or Skipped — persist the timestamp so biometric restarts inherit it.
                    prefs.edit().putLong(entityType, now).apply()
                    Log.d(TAG, "sync success: $entityType")
                }
            }
        } finally {
            if (activeSyncs.decrementAndGet() == 0) _isSyncing.value = false
        }
    }
}
