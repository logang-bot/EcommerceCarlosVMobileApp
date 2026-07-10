package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restrusher.ecomercecarlosv.data.local.entity.PagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {
    @Query("SELECT * FROM pagos WHERE pedidoId = :pedidoId ORDER BY paidAt ASC")
    fun getByPedidoFlow(pedidoId: String): Flow<List<PagoEntity>>

    @Query("SELECT * FROM pagos WHERE pedidoId = :pedidoId ORDER BY paidAt ASC")
    suspend fun getByPedido(pedidoId: String): List<PagoEntity>

    @Query(
        """SELECT pagos.* FROM pagos
        INNER JOIN pedidos ON pagos.pedidoId = pedidos.id
        WHERE pedidos.clienteId = :clienteId
        ORDER BY pagos.paidAt ASC""",
    )
    fun getByClienteFlow(clienteId: String): Flow<List<PagoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pago: PagoEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(pagos: List<PagoEntity>)

    @Query("DELETE FROM pagos WHERE pedidoId = :pedidoId")
    suspend fun deleteByPedido(pedidoId: String)
}
