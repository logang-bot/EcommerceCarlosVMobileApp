package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restrusher.ecomercecarlosv.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes WHERE mercadoId = :mercadoId AND isBlacklisted = 0 ORDER BY name ASC")
    fun getByMercado(mercadoId: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: ClienteEntity)

    @Update
    suspend fun update(cliente: ClienteEntity)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun deleteById(id: String)
}
