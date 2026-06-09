package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY createdAt DESC")
    fun getByCliente(clienteId: String): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: String): Flow<PedidoEntity?>

    @Query("SELECT * FROM pedidos WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PedidoEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pedido: PedidoEntity)

    @Query("UPDATE pedidos SET status = :status, paid = :paid, paidAt = :paidAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, paid: Double, paidAt: Long?)

    @Query("SELECT * FROM pedidos WHERE status != 'PAID'")
    fun getAllUnpaid(): Flow<List<PedidoEntity>>

    @Query("UPDATE pedidos SET createdAt = :createdAt WHERE id = :id")
    suspend fun updateDate(id: String, createdAt: Long)

    @Query("DELETE FROM pedidos WHERE id = :id")
    suspend fun deleteById(id: String)
}
