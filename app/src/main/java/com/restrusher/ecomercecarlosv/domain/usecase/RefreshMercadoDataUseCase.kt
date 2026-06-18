package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Refreshes the three entities that drive the mercado dashboard in parallel.
 * The mercado overview shows per-market client counts and payment warnings,
 * so all three must be current to render correctly. Returns true only if all succeed.
 */
class RefreshMercadoDataUseCase @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    private val clienteRepository: ClienteRepository,
    private val pedidoRepository: PedidoRepository,
) {
    suspend operator fun invoke(): Boolean = coroutineScope {
        val mercados = async { mercadoRepository.refresh() }
        val clientes = async { clienteRepository.refresh() }
        val pedidos  = async { pedidoRepository.refresh() }
        mercados.await() && clientes.await() && pedidos.await()
    }
}
