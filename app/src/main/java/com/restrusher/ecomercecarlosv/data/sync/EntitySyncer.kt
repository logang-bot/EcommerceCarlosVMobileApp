package com.restrusher.ecomercecarlosv.data.sync

sealed class SyncResult {
    object Success : SyncResult()
    data class Failure(val error: Throwable) : SyncResult()
    object Skipped : SyncResult()
}

interface EntitySyncer {
    /** [since] = epoch-ms of last successful sync. 0 means first run → full fetch. */
    suspend fun sync(since: Long = 0L): SyncResult
}
