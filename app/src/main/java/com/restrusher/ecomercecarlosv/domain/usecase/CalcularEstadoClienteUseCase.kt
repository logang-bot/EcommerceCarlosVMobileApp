package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import javax.inject.Inject

/**
 * Derives the traffic-light status shown for a cliente.
 *
 * Only *partially paid* regular pedidos count towards it: an untouched PENDING pedido is a normal
 * open order, and a saldo extra is manually recorded debt rather than an unpaid delivery. Both
 * still appear in the balance the screens display — they just do not colour the client red.
 */
class CalcularEstadoClienteUseCase @Inject constructor() {

    /** @param now overridable so the age rule can be tested exactly; callers take the default. */
    operator fun invoke(
        pedidos: List<Pedido>,
        umbrales: Umbrales,
        now: Long = System.currentTimeMillis(),
    ): ClientStatus {
        val enMora = pedidos.filter { it.countsTowardStatus }
        val statusBalance = enMora.sumOf { it.pending }
        if (statusBalance <= 0.0) return ClientStatus.AL_DIA
        val hasOldUnpaid = enMora.any { isOlderThan(it.createdAt, umbrales.diasMaximos, now) }
        val isCritical = hasOldUnpaid || statusBalance > umbrales.montoMaximo
        return if (isCritical) ClientStatus.CRITICO else ClientStatus.ADVERTENCIA
    }

    private val Pedido.countsTowardStatus: Boolean
        get() = status == PedidoStatus.PARTIAL && !isSaldoExtra

    private fun isOlderThan(createdAt: Long, days: Int, now: Long): Boolean =
        (now - createdAt) > days.toLong() * 24 * 60 * 60 * 1000
}
