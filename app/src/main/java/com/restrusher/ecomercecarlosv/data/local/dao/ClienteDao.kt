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
    @Query("SELECT * FROM clientes WHERE mercadoId = :mercadoId AND isBlacklisted = 0 AND isDeleted = 0 ORDER BY name ASC")
    fun getByMercado(mercadoId: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE isBlacklisted = 0 AND isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllIncludingBlacklisted(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE isBlacklisted = 1 AND isDeleted = 0 ORDER BY blacklistedAt DESC")
    fun getBlacklisted(): Flow<List<ClienteEntity>>

    @Query("UPDATE clientes SET isBlacklisted = 1, blacklistReason = :reason, blacklistBalance = :balance, blacklistedAt = :at, blacklistIsManualAmount = :isManualAmount WHERE id = :id")
    suspend fun blacklist(id: String, reason: String, balance: Double, at: Long, isManualAmount: Boolean)

    @Query("UPDATE clientes SET isBlacklisted = 0, blacklistReason = NULL, blacklistBalance = 0, blacklistIsManualAmount = 0, blacklistedAt = NULL WHERE id = :id")
    suspend fun unblacklist(id: String)

    @Query("SELECT * FROM clientes WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getByIdFlow(id: String): Flow<ClienteEntity?>

    @Query("SELECT * FROM clientes WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getById(id: String): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cliente: ClienteEntity): Long

    @Update
    suspend fun update(cliente: ClienteEntity)

    @Query("UPDATE clientes SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: String)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun deleteById(id: String)
}
