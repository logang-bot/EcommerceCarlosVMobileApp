package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MercadoDao {
    @Query("SELECT * FROM mercados ORDER BY name ASC")
    fun getAll(): Flow<List<MercadoEntity>>

    @Query("SELECT * FROM mercados WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MercadoEntity?

    @Query("SELECT * FROM mercados WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: String): Flow<MercadoEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mercado: MercadoEntity): Long

    @Update
    suspend fun update(mercado: MercadoEntity)

    @Query("DELETE FROM mercados WHERE id = :id")
    suspend fun deleteById(id: String)
}
