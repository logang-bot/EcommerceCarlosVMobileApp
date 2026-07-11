package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UmbralesDao {
    @Query("SELECT * FROM umbrales WHERE id = 'global' LIMIT 1")
    fun getFlow(): Flow<UmbralesEntity?>

    @Query("SELECT * FROM umbrales WHERE id = 'global' LIMIT 1")
    suspend fun get(): UmbralesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(umbrales: UmbralesEntity)
}
