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

    @Query("SELECT * FROM clientes WHERE isBlacklisted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE isBlacklisted = 1 ORDER BY blacklistedAt DESC")
    fun getBlacklisted(): Flow<List<ClienteEntity>>

    @Query("UPDATE clientes SET isBlacklisted = 1, blacklistReason = :reason, blacklistBalance = :balance, blacklistedAt = :at WHERE id = :id")
    suspend fun blacklist(id: String, reason: String, balance: Double, at: Long)

    @Query("UPDATE clientes SET isBlacklisted = 0, blacklistReason = NULL, blacklistBalance = 0, blacklistedAt = NULL WHERE id = :id")
    suspend fun unblacklist(id: String)

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: String): Flow<ClienteEntity?>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: ClienteEntity)

    @Update
    suspend fun update(cliente: ClienteEntity)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun deleteById(id: String)
}
