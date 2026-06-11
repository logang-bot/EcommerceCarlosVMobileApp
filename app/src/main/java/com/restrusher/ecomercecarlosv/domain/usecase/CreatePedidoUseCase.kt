package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.ui.screen.pedido.CartItem
import java.util.UUID
import javax.inject.Inject

class CreatePedidoUseCase @Inject constructor(
    private val pedidoRepository: PedidoRepository,
) {
    suspend operator fun invoke(
        clienteId: String,
        items: List<CartItem>,
        initialPayment: Double,
    ): String {
        val total = items.sumOf { it.unitPrice * it.quantity }
        val status = when {
            initialPayment >= total -> PedidoStatus.PAID
            initialPayment > 0 -> PedidoStatus.PARTIAL
            else -> PedidoStatus.PENDING
        }
        val now = System.currentTimeMillis()
        val pedidoId = UUID.randomUUID().toString()

        val pedido = Pedido(
            id = pedidoId,
            clienteId = clienteId,
            status = status,
            total = total,
            paid = initialPayment,
            createdAt = now,
            paidAt = if (status == PedidoStatus.PAID) now else null,
            itemCount = items.size,
        )

        val detalles = items.map { item ->
            DetallePedido(
                id = UUID.randomUUID().toString(),
                pedidoId = pedidoId,
                productoId = item.productoId,
                productName = item.productName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                catalogPrice = item.catalogPrice,
                notes = item.notes,
            )
        }

        pedidoRepository.create(pedido, detalles)
        return pedidoId
    }
}
