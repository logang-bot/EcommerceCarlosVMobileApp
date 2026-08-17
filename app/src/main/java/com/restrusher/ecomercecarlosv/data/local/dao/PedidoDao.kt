package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getByCliente(clienteId: String): Flow<List<PedidoEntity>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getByClienteWithLines(clienteId: String): Flow<List<PedidoWithLines>>

    @Query("SELECT * FROM pedidos WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getByIdFlow(id: String): Flow<PedidoEntity?>

    @Query("SELECT * FROM pedidos WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getById(id: String): PedidoEntity?

    /** Row presence only — deliberately ignores `isDeleted`, since a tombstone still satisfies the
     *  `detalle_pedido.pedidoId` and `pagos.pedidoId` foreign keys. */
    @Query("SELECT id FROM pedidos WHERE id IN (:ids)")
    suspend fun existingIds(ids: List<String>): List<String>

    /** Returns -1 when the row already exists, so callers can fall back to [update]. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pedido: PedidoEntity): Long

    @Update
    suspend fun update(pedido: PedidoEntity)

    @Query("UPDATE pedidos SET status = :status, paid = :paid, paidAt = :paidAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, paid: Double, paidAt: Long?)

    @Query("SELECT * FROM pedidos WHERE status != 'PAID' AND isDeleted = 0")
    fun getAllUnpaid(): Flow<List<PedidoEntity>>

    @Query("UPDATE pedidos SET createdAt = :createdAt WHERE id = :id")
    suspend fun updateDate(id: String, createdAt: Long)

    @Query("SELECT * FROM pedidos WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PedidoEntity>>

    @Query("UPDATE pedidos SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: String)

    @Query("UPDATE pedidos SET isDeleted = 1 WHERE clienteId = :clienteId")
    suspend fun softDeleteByCliente(clienteId: String)

    @Query("UPDATE pedidos SET isDeleted = 1 WHERE clienteId IN (SELECT id FROM clientes WHERE mercadoId = :mercadoId)")
    suspend fun softDeleteByMercado(mercadoId: String)

    @Query("DELETE FROM pedidos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE pedidos SET status = 'PAID', paid = total, paidAt = :paidAt WHERE clienteId = :clienteId AND status != 'PAID' AND isDeleted = 0")
    suspend fun markAllPaidForCliente(clienteId: String, paidAt: Long)

    /** The rows [markAllPaidForCliente] is about to settle — same predicate, so the two cannot disagree. */
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId AND status != 'PAID' AND isDeleted = 0")
    suspend fun unpaidForCliente(clienteId: String): List<PedidoEntity>

    @Query("UPDATE pedidos SET total = :total, itemCount = :itemCount, status = :status, paid = :paid, paidAt = :paidAt WHERE id = :id")
    suspend fun updateAfterEdit(id: String, total: Double, itemCount: Int, status: String, paid: Double, paidAt: Long?)

    @Query("SELECT COUNT(*) FROM pedidos WHERE createdAt < :cutoff")
    suspend fun countByCreatedAtBefore(cutoff: Long): Int

    @Query("DELETE FROM pedidos WHERE createdAt < :cutoff")
    suspend fun deleteByCreatedAtBefore(cutoff: Long)
}
