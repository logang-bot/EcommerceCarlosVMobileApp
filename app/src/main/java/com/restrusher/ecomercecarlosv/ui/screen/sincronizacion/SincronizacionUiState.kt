package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import com.restrusher.ecomercecarlosv.ui.common.SyncIconState

data class SincronizacionUiState(
    val syncState: SyncIconState = SyncIconState.SYNCED,
    val items: List<SyncQueueItem> = emptyList(),
    val lastSyncedAt: Long? = null,
)

data class SyncQueueItem(
    val id: Long,
    val entityType: String,
    val operation: String,
    val entityLabel: String,
    val createdAt: Long,
    val retryCount: Int,
)
