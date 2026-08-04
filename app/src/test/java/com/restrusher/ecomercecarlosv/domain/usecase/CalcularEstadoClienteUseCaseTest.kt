package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.fixtures.pedido
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularEstadoClienteUseCaseTest {

    private val useCase = CalcularEstadoClienteUseCase()

    private val ahora = 1_700_000_000_000L
    private val umbrales = Umbrales(montoMaximo = 200.0, diasMaximos = 30)

    private fun diasAtras(days: Int): Long = ahora - days.toLong() * 24 * 60 * 60 * 1000

    /** A partially paid regular pedido — the only kind that drives the status. */
    private fun enMora(pending: Double, createdAt: Long = ahora) =
        pedido(status = PedidoStatus.PARTIAL, total = pending, paid = 0.0, createdAt = createdAt)

    private fun estado(vararg pedidos: Pedido) = useCase(pedidos.toList(), umbrales, ahora)

    @Test
    fun `sin pedidos — AL_DIA`() {
        assertEquals(ClientStatus.AL_DIA, estado())
    }

    @Test
    fun `nothing partially paid — AL_DIA`() {
        val result = estado(pedido(status = PedidoStatus.PAID, total = 500.0, paid = 500.0))

        assertEquals(ClientStatus.AL_DIA, result)
    }

    @Test
    fun `debt within both thresholds — ADVERTENCIA`() {
        assertEquals(ClientStatus.ADVERTENCIA, estado(enMora(pending = 150.0)))
    }

    @Test
    fun `debt exactly at the amount threshold — ADVERTENCIA, since the rule is strictly greater`() {
        assertEquals(ClientStatus.ADVERTENCIA, estado(enMora(pending = 200.0)))
    }

    @Test
    fun `debt above the amount threshold — CRITICO`() {
        assertEquals(ClientStatus.CRITICO, estado(enMora(pending = 200.01)))
    }

    @Test
    fun `debt summed across several pedidos crosses the threshold — CRITICO`() {
        val result = estado(enMora(pending = 120.0), enMora(pending = 120.0))

        assertEquals(ClientStatus.CRITICO, result)
    }

    @Test
    fun `a small debt older than the day threshold — CRITICO regardless of amount`() {
        val result = estado(enMora(pending = 5.0, createdAt = diasAtras(31)))

        assertEquals(ClientStatus.CRITICO, result)
    }

    @Test
    fun `a debt exactly at the day threshold — ADVERTENCIA, since the rule is strictly older`() {
        val result = estado(enMora(pending = 5.0, createdAt = diasAtras(30)))

        assertEquals(ClientStatus.ADVERTENCIA, result)
    }

    @Test
    fun `an old PENDING pedido — does not trigger CRITICO, only PARTIAL counts`() {
        val viejoPendiente = pedido(
            status = PedidoStatus.PENDING,
            total = 500.0,
            paid = 0.0,
            createdAt = diasAtras(90),
        )

        assertEquals(ClientStatus.AL_DIA, estado(viejoPendiente))
    }

    @Test
    fun `an old saldo extra — does not trigger CRITICO, manual debt is not an unpaid delivery`() {
        val viejoSaldoExtra = pedido(
            status = PedidoStatus.PARTIAL,
            total = 500.0,
            paid = 0.0,
            createdAt = diasAtras(90),
            isSaldoExtra = true,
        )

        assertEquals(ClientStatus.AL_DIA, estado(viejoSaldoExtra))
    }

    @Test
    fun `a saldo extra never counts towards the amount threshold either`() {
        val saldoExtraGrande = pedido(
            status = PedidoStatus.PARTIAL,
            total = 5_000.0,
            paid = 0.0,
            isSaldoExtra = true,
        )

        assertEquals(ClientStatus.AL_DIA, estado(saldoExtraGrande))
    }

    @Test
    fun `an old saldo extra alongside a recent small debt — stays ADVERTENCIA`() {
        val result = estado(
            enMora(pending = 10.0),
            pedido(
                status = PedidoStatus.PARTIAL,
                total = 500.0,
                paid = 0.0,
                createdAt = diasAtras(90),
                isSaldoExtra = true,
            ),
        )

        assertEquals(ClientStatus.ADVERTENCIA, result)
    }

    @Test
    fun `a fully paid pedido contributes no pending balance`() {
        val result = estado(
            pedido(status = PedidoStatus.PARTIAL, total = 100.0, paid = 100.0),
        )

        assertEquals(ClientStatus.AL_DIA, result)
    }

    @Test
    fun `raising the configured thresholds downgrades a client from CRITICO`() {
        val pedidos = listOf(enMora(pending = 300.0))

        assertEquals(ClientStatus.CRITICO, useCase(pedidos, Umbrales(montoMaximo = 200.0), ahora))
        assertEquals(ClientStatus.ADVERTENCIA, useCase(pedidos, Umbrales(montoMaximo = 500.0), ahora))
    }

    @Test
    fun `the default umbrales are 200 bolivianos and 30 days`() {
        val defaults = Umbrales()

        assertEquals(200.0, defaults.montoMaximo, 0.001)
        assertEquals(30, defaults.diasMaximos)
    }

    @Test
    fun `now defaults to the current time when the caller omits it`() {
        val recienCreado = enMora(pending = 10.0, createdAt = System.currentTimeMillis())

        assertEquals(ClientStatus.ADVERTENCIA, useCase(listOf(recienCreado), umbrales))
    }
}
