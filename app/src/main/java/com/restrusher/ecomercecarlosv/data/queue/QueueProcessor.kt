package com.restrusher.ecomercecarlosv.data.queue

import android.net.Uri
import android.util.Log
import androidx.work.WorkManager
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.mapper.ClienteMapper
import com.restrusher.ecomercecarlosv.data.mapper.DetallePedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.MercadoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.ProductoMapper
import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.data.remote.StorageService
import com.restrusher.ecomercecarlosv.di.ApplicationScope
import com.restrusher.ecomercecarlosv.domain.error.AppError
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueProcessor @Inject constructor(
    private val syncOperationDao: SyncOperationDao,
    private val mercadoDao: MercadoDao,
    private val clienteDao: ClienteDao,
    private val productoDao: ProductoDao,
    private val pedidoDao: PedidoDao,
    private val detalleDao: DetallePedidoDao,
    private val networkMonitor: NetworkMonitor,
    private val supabase: SupabaseClient,
    private val storageService: StorageService,
    private val errorHandler: GlobalErrorHandler,
    private val workManager: WorkManager,
    @ApplicationScope private val appScope: CoroutineScope,
) {

    private val _lastSuccessfulFlushAt = MutableStateFlow<Long?>(null)
    val lastSuccessfulFlushAt: StateFlow<Long?> = _lastSuccessfulFlushAt.asStateFlow()

    fun start() {
        // Flush when connectivity is restored after being offline.
        appScope.launch {
            networkMonitor.isOnlineFlow
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline) {
                        Log.d(TAG, "online — flushing queue")
                        flush()
                    }
                }
        }
        // On every new enqueue: ensure a WorkManager job exists (survives app kill) and, if
        // already online, flush immediately without waiting for the worker to fire.
        appScope.launch {
            syncOperationDao.observeLatestEnqueuedId()
                .distinctUntilChanged()
                .filter { it > 0 }
                .collect {
                    SyncWorker.schedule(workManager)
                    if (networkMonitor.isOnline) {
                        Log.d(TAG, "new queue entry detected — flushing immediately")
                        flush()
                    }
                }
        }
    }

    // Returns true if at least one operation failed to push. SyncWorker uses this to decide
    // whether to return Result.retry() so WorkManager can apply exponential backoff.
    suspend fun flush(): Boolean {
        val pending = syncOperationDao.getPending()
        if (pending.isEmpty()) return false

        val deduplicated = deduplicate(pending)
        Log.d(TAG, "flush: ${pending.size} raw ops → ${deduplicated.size} after dedup")

        var anyFailed = false
        for (op in deduplicated) {
            val success = processOperation(op)
            if (success) {
                pending
                    .filter { it.entityType == op.entityType && it.entityId == op.entityId }
                    .forEach { syncOperationDao.delete(it.id) }
            } else {
                anyFailed = true
                pending
                    .filter { it.entityType == op.entityType && it.entityId == op.entityId }
                    .forEach { syncOperationDao.incrementRetry(it.id) }
            }
        }
        if (!anyFailed) {
            _lastSuccessfulFlushAt.value = System.currentTimeMillis()
        }
        return anyFailed
    }

    fun triggerFlush() {
        appScope.launch {
            if (networkMonitor.isOnline) flush()
        }
    }

    private fun deduplicate(ops: List<SyncOperationEntity>): List<SyncOperationEntity> {
        return ops
            .groupBy { it.entityType to it.entityId }
            .map { (_, group) ->
                // DELETE takes priority; within same op type, use latest
                group.firstOrNull { it.operation == SyncOp.DELETE }
                    ?: group.maxByOrNull { it.createdAt }!!
            }
    }

    private suspend fun processOperation(op: SyncOperationEntity): Boolean {
        return runCatching {
            when (op.operation) {
                SyncOp.UPSERT -> upsert(op)
                SyncOp.DELETE -> delete(op)
                else -> {
                    Log.w(TAG, "unknown operation '${op.operation}' — skipping")
                    true
                }
            }
        }.onFailure { e ->
            val error = AppError.Queue(
                "Failed to push ${op.entityType}(${op.entityId}) to Supabase",
                e,
            )
            errorHandler.emit(error)
        }.getOrDefault(false)
    }

    private suspend fun upsert(op: SyncOperationEntity): Boolean {
        when (op.entityType) {
            EntityType.MERCADO -> {
                val entity = mercadoDao.getById(op.entityId) ?: return true // deleted locally
                val finalEntity = if (entity.photoUrl?.startsWith("content://") == true) {
                    val remoteUrl = storageService.uploadPhoto("mercado-photos", entity.id, Uri.parse(entity.photoUrl))
                    entity.copy(photoUrl = remoteUrl).also { mercadoDao.update(it) }
                } else {
                    entity
                }
                supabase.from("mercados").upsert(MercadoMapper.toDto(finalEntity))
            }
            EntityType.CLIENTE -> {
                val entity = clienteDao.getById(op.entityId) ?: return true
                val finalEntity = if (entity.photoUrl?.startsWith("content://") == true) {
                    val remoteUrl = storageService.uploadPhoto("cliente-photos", entity.id, Uri.parse(entity.photoUrl))
                    entity.copy(photoUrl = remoteUrl).also { clienteDao.update(it) }
                } else {
                    entity
                }
                supabase.from("clientes").upsert(ClienteMapper.toDto(finalEntity))
            }
            EntityType.PRODUCTO -> {
                val entity = productoDao.getById(op.entityId) ?: return true
                val finalEntity = if (entity.photoUrl?.startsWith("content://") == true) {
                    val remoteUrl = storageService.uploadPhoto("producto-photos", entity.id, Uri.parse(entity.photoUrl))
                    entity.copy(photoUrl = remoteUrl).also { productoDao.update(it) }
                } else {
                    entity
                }
                supabase.from("productos").upsert(ProductoMapper.toDto(finalEntity))
            }
            EntityType.PEDIDO -> {
                val entity = pedidoDao.getById(op.entityId) ?: return true
                supabase.from("pedidos").upsert(PedidoMapper.toDto(entity))
                // Also push current line items (replace remote ones)
                val detalles = detalleDao.getByPedido(op.entityId)
                if (detalles.isNotEmpty()) {
                    supabase.from("detalle_pedido")
                        .delete { filter { eq("pedido_id", op.entityId) } }
                    supabase.from("detalle_pedido")
                        .upsert(detalles.map(DetallePedidoMapper::toDto))
                }
            }
            else -> Log.w(TAG, "upsert: unknown entityType '${op.entityType}'")
        }
        Log.d(TAG, "upsert: pushed ${op.entityType}(${op.entityId})")
        return true
    }

    private suspend fun delete(op: SyncOperationEntity): Boolean {
        val table = when (op.entityType) {
            EntityType.MERCADO  -> "mercados"
            EntityType.CLIENTE  -> "clientes"
            EntityType.PRODUCTO -> "productos"
            EntityType.PEDIDO   -> "pedidos"
            else -> {
                Log.w(TAG, "delete: unknown entityType '${op.entityType}'")
                return true
            }
        }
        supabase.from(table).update({ set("is_deleted", true) }) { filter { eq("id", op.entityId) } }
        Log.d(TAG, "soft-delete: marked ${op.entityType}(${op.entityId}) deleted in $table")
        return true
    }

    companion object {
        private const val TAG = "QueueProcessor"
    }
}
