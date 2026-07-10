package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pago
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    val isSyncing: Flow<Boolean>
    suspend fun refresh(): Boolean
    fun getByCliente(clienteId: String): Flow<List<Pedido>>
    fun getByClienteWithLines(clienteId: String): Flow<List<Pedido>>
    fun getAll(): Flow<List<Pedido>>
    fun getAllUnpaid(): Flow<List<Pedido>>
    fun getByIdFlow(id: String): Flow<Pedido?>
    suspend fun getById(id: String): Pedido?
    suspend fun getDetallesByPedido(pedidoId: String): List<DetallePedido>
    fun getDetallesByPedidoFlow(pedidoId: String): Flow<List<DetallePedido>>
    fun getPagosByPedidoFlow(pedidoId: String): Flow<List<Pago>>
    fun getPagosByClienteFlow(clienteId: String): Flow<List<Pago>>
    suspend fun create(pedido: Pedido, detalles: List<DetallePedido>)
    suspend fun updateStatus(id: String, status: PedidoStatus, paid: Double, paidAt: Long?)
    suspend fun updateDate(id: String, createdAt: Long)
    suspend fun updateLines(pedidoId: String, detalles: List<DetallePedido>, newTotal: Double, paid: Double, paidAt: Long?)
    suspend fun registrarPago(pago: Pago, newPaid: Double, newStatus: PedidoStatus)
    suspend fun delete(id: String)
    suspend fun markAllPaidForCliente(clienteId: String)
}
