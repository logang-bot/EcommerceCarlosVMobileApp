package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory stand-in for the queue table. Room's real behaviour is covered in phase 5. */
class FakeSyncOperationDao : SyncOperationDao {

    private val operations = MutableStateFlow<List<SyncOperationEntity>>(emptyList())

    fun given(vararg ops: SyncOperationEntity) {
        operations.value = ops.sortedBy { it.createdAt }
    }

    override suspend fun enqueue(op: SyncOperationEntity): Long {
        operations.value = (operations.value + op).sortedBy { it.createdAt }
        return op.id
    }

    override suspend fun getPending(): List<SyncOperationEntity> = operations.value

    override suspend fun delete(id: Long) {
        operations.value = operations.value.filterNot { it.id == id }
    }

    override suspend fun incrementRetry(id: Long) {
        operations.value = operations.value.map {
            if (it.id == id) it.copy(retryCount = it.retryCount + 1) else it
        }
    }

    override suspend fun deduplicateUpserts(entityType: String, entityId: String) {
        operations.value = operations.value.filterNot {
            it.entityType == entityType && it.entityId == entityId && it.operation != "DELETE"
        }
    }

    override suspend fun pendingCount(): Int = operations.value.size

    override fun observeLatestEnqueuedId(): Flow<Long> =
        operations.map { ops -> ops.maxOfOrNull { it.id } ?: 0L }

    override suspend fun resetAllRetryCount() {
        operations.value = operations.value.map { it.copy(retryCount = 0) }
    }

    override fun observeAll(): Flow<List<SyncOperationEntity>> = operations
}
