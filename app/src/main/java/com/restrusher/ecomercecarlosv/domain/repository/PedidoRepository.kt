package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    fun getByCliente(clienteId: String): Flow<List<Pedido>>
    fun getByClienteWithLines(clienteId: String): Flow<List<Pedido>>
    fun getAll(): Flow<List<Pedido>>
    fun getAllUnpaid(): Flow<List<Pedido>>
    fun getByIdFlow(id: String): Flow<Pedido?>
    suspend fun getById(id: String): Pedido?
    suspend fun getDetallesByPedido(pedidoId: String): List<DetallePedido>
    fun getDetallesByPedidoFlow(pedidoId: String): Flow<List<DetallePedido>>
    suspend fun create(pedido: Pedido, detalles: List<DetallePedido>)
    suspend fun updateStatus(id: String, status: PedidoStatus, paid: Double, paidAt: Long?)
    suspend fun updateDate(id: String, createdAt: Long)
    suspend fun updateLines(pedidoId: String, detalles: List<DetallePedido>, newTotal: Double, paid: Double, paidAt: Long?)
    suspend fun delete(id: String)
    suspend fun markAllPaidForCliente(clienteId: String)
}
