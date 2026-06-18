package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Refreshes clientes and pedidos in parallel. Both are business data (30-min threshold)
 * and must stay in sync — client status is computed from their unpaid pedidos.
 * Returns true only if both succeed.
 */
class RefreshClienteDataUseCase @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val pedidoRepository: PedidoRepository,
) {
    suspend operator fun invoke(): Boolean = coroutineScope {
        val clientes = async { clienteRepository.refresh() }
        val pedidos  = async { pedidoRepository.refresh() }
        clientes.await() && pedidos.await()
    }
}
