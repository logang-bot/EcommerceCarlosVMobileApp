package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restrusher.ecomercecarlosv.data.local.entity.DetallePedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetallePedidoDao {
    @Query("SELECT * FROM detalle_pedido WHERE pedidoId = :pedidoId")
    suspend fun getByPedido(pedidoId: String): List<DetallePedidoEntity>

    @Query("SELECT * FROM detalle_pedido WHERE pedidoId = :pedidoId")
    fun getByPedidoFlow(pedidoId: String): Flow<List<DetallePedidoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(detalles: List<DetallePedidoEntity>)

    @Query("DELETE FROM detalle_pedido WHERE pedidoId = :pedidoId")
    suspend fun deleteByPedido(pedidoId: String)
}
