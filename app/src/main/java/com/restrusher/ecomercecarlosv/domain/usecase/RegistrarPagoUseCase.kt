package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.Pago
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import java.util.UUID
import javax.inject.Inject

class RegistrarPagoUseCase @Inject constructor(
    private val pedidoRepository: PedidoRepository,
) {
    suspend operator fun invoke(pedido: Pedido, amount: Double) {
        val newPaid = (pedido.paid + amount).coerceAtMost(pedido.total)
        val newStatus = if (newPaid >= pedido.total) PedidoStatus.PAID else PedidoStatus.PARTIAL
        val pago = Pago(
            id = UUID.randomUUID().toString(),
            pedidoId = pedido.id,
            amount = amount,
            paidAt = System.currentTimeMillis(),
        )
        pedidoRepository.registrarPago(pago, newPaid, newStatus)
    }
}
