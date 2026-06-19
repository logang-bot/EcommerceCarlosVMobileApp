package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos WHERE isActive = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getById(id: String): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: ProductoEntity)

    @Update
    suspend fun update(producto: ProductoEntity)

    @Query("UPDATE productos SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: String)

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun deleteById(id: String)
}
