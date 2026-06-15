package com.restrusher.ecomercecarlosv.data.sync

sealed class SyncResult {
    object Success : SyncResult()
    data class Failure(val error: Throwable) : SyncResult()
    object Skipped : SyncResult()
}

interface EntitySyncer {
    suspend fun sync(): SyncResult
}
