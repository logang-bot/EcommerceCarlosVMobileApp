package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import java.util.UUID
import javax.inject.Inject

class CreateSaldoExtraUseCase @Inject constructor(
    private val pedidoRepository: PedidoRepository,
) {
    suspend operator fun invoke(
        clienteId: String,
        description: String,
        amount: Double,
        date: Long,
    ): String {
        val pedidoId = UUID.randomUUID().toString()
        pedidoRepository.create(
            pedido = Pedido(
                id = pedidoId,
                clienteId = clienteId,
                status = PedidoStatus.PENDING,
                total = amount,
                paid = 0.0,
                notes = description,
                createdAt = date,
                isSaldoExtra = true,
            ),
            detalles = emptyList(),
        )
        return pedidoId
    }
}
