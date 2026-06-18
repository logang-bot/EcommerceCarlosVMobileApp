package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(op: SyncOperationEntity): Long

    @Query("SELECT * FROM sync_operations ORDER BY createdAt ASC")
    suspend fun getPending(): List<SyncOperationEntity>

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE sync_operations SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("DELETE FROM sync_operations WHERE entityType = :entityType AND entityId = :entityId AND operation != 'DELETE'")
    suspend fun deduplicateUpserts(entityType: String, entityId: String)

    @Query("SELECT COUNT(*) FROM sync_operations")
    suspend fun pendingCount(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM sync_operations")
    fun observeLatestEnqueuedId(): Flow<Long>

    @Query("UPDATE sync_operations SET retryCount = 0")
    suspend fun resetAllRetryCount()

    @Query("SELECT * FROM sync_operations ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<SyncOperationEntity>>
}
