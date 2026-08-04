package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pago
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Records what the use cases persist. [created] and [registeredPagos] are what the assertions read;
 * everything else is a no-op stub so the interface stays satisfied.
 */
class FakePedidoRepository : PedidoRepository {

    data class CreatedPedido(val pedido: Pedido, val detalles: List<DetallePedido>)

    data class RegisteredPago(val pago: Pago, val newPaid: Double, val newStatus: PedidoStatus)

    val created = mutableListOf<CreatedPedido>()
    val registeredPagos = mutableListOf<RegisteredPago>()

    /** What [refresh] returns, and how many times it was called. */
    var refreshResult = true
    var refreshCount = 0
        private set

    private val pedidos = MutableStateFlow<List<Pedido>>(emptyList())

    fun givenPedidos(vararg items: Pedido) {
        pedidos.value = items.toList()
    }

    override val isSyncing: Flow<Boolean> = flowOf(false)

    override suspend fun refresh(): Boolean {
        refreshCount++
        return refreshResult
    }

    override fun getByCliente(clienteId: String): Flow<List<Pedido>> =
        pedidos.map { all -> all.filter { it.clienteId == clienteId } }

    override fun getByClienteWithLines(clienteId: String): Flow<List<Pedido>> = getByCliente(clienteId)

    override fun getAll(): Flow<List<Pedido>> = pedidos

    override fun getAllUnpaid(): Flow<List<Pedido>> =
        pedidos.map { all -> all.filter { it.status != PedidoStatus.PAID } }

    override fun getByIdFlow(id: String): Flow<Pedido?> = pedidos.map { all -> all.find { it.id == id } }

    override suspend fun getById(id: String): Pedido? = pedidos.value.find { it.id == id }

    override suspend fun getDetallesByPedido(pedidoId: String): List<DetallePedido> =
        created.find { it.pedido.id == pedidoId }?.detalles.orEmpty()

    override fun getDetallesByPedidoFlow(pedidoId: String): Flow<List<DetallePedido>> = flowOf(emptyList())

    override fun getPagosByPedidoFlow(pedidoId: String): Flow<List<Pago>> = flowOf(emptyList())

    override fun getPagosByClienteFlow(clienteId: String): Flow<List<Pago>> = flowOf(emptyList())

    override suspend fun create(pedido: Pedido, detalles: List<DetallePedido>) {
        created += CreatedPedido(pedido, detalles)
        pedidos.value = pedidos.value + pedido
    }

    override suspend fun updateStatus(id: String, status: PedidoStatus, paid: Double, paidAt: Long?) = Unit

    override suspend fun updateDate(id: String, createdAt: Long) = Unit

    override suspend fun updateLines(
        pedidoId: String,
        detalles: List<DetallePedido>,
        newTotal: Double,
        paid: Double,
        paidAt: Long?,
    ) = Unit

    override suspend fun registrarPago(pago: Pago, newPaid: Double, newStatus: PedidoStatus) {
        registeredPagos += RegisteredPago(pago, newPaid, newStatus)
    }

    override suspend fun delete(id: String) = Unit

    override suspend fun markAllPaidForCliente(clienteId: String) = Unit
}
