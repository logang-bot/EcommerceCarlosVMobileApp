package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.queue.QueueProcessor
import com.restrusher.ecomercecarlosv.ui.common.SyncIconState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SincronizacionViewModel @Inject constructor(
    private val syncOperationDao: SyncOperationDao,
    private val queueProcessor: QueueProcessor,
) : ViewModel() {

    val uiState = combine(
        syncOperationDao.observeAll(),
        queueProcessor.lastSuccessfulFlushAt,
    ) { ops, lastFlush ->
        val items = ops.map { op ->
            SyncQueueItem(
                id = op.id,
                entityType = op.entityType,
                operation = op.operation,
                entityLabel = op.entityLabel,
                createdAt = op.createdAt,
                retryCount = op.retryCount,
            )
        }
        val syncState = when {
            items.isEmpty() -> SyncIconState.SYNCED
            items.any { it.retryCount > 0 } -> SyncIconState.ERROR
            else -> SyncIconState.PENDING
        }
        SincronizacionUiState(
            syncState = syncState,
            items = items,
            lastSyncedAt = lastFlush,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SincronizacionUiState(),
    )

    fun onRetry() {
        viewModelScope.launch {
            queueProcessor.triggerFlush()
        }
    }
}
