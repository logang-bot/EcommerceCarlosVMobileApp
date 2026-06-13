package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY createdAt DESC")
    fun getByCliente(clienteId: String): Flow<List<PedidoEntity>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY createdAt DESC")
    fun getByClienteWithLines(clienteId: String): Flow<List<PedidoWithLines>>

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

    @Query("SELECT * FROM pedidos ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PedidoEntity>>

    @Query("DELETE FROM pedidos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE pedidos SET status = 'PAID', paid = total, paidAt = :paidAt WHERE clienteId = :clienteId AND status != 'PAID'")
    suspend fun markAllPaidForCliente(clienteId: String, paidAt: Long)

    @Query("UPDATE pedidos SET total = :total, itemCount = :itemCount, status = :status, paid = :paid, paidAt = :paidAt WHERE id = :id")
    suspend fun updateAfterEdit(id: String, total: Double, itemCount: Int, status: String, paid: Double, paidAt: Long?)
}
