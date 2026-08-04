package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import com.restrusher.ecomercecarlosv.fixtures.pedido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgregarListaNegraUiStateTest {

    private val dosPedidos = listOf(
        pedido(id = "p1", total = 100.0, paid = 40.0),
        pedido(id = "p2", total = 50.0, paid = 0.0),
    )

    @Test
    fun `autoAmount — sums the pending balance of every pedido`() {
        val subject = AgregarListaNegraUiState(pendingPedidos = dosPedidos)

        assertEquals(110.0, subject.autoAmount, 0.001)
    }

    @Test
    fun `autoAmount — no pending pedidos — is zero`() {
        assertEquals(0.0, AgregarListaNegraUiState().autoAmount, 0.001)
    }

    @Test
    fun `effectiveAmount — auto mode — ignores the manual amount`() {
        val subject = AgregarListaNegraUiState(
            pendingPedidos = dosPedidos,
            totalMode = TotalMode.AUTO,
            manualAmount = "999",
        )

        assertEquals(110.0, subject.effectiveAmount, 0.001)
    }

    @Test
    fun `effectiveAmount — manual mode — parses the typed amount`() {
        val subject = AgregarListaNegraUiState(
            pendingPedidos = dosPedidos,
            totalMode = TotalMode.MANUAL,
            manualAmount = "75.5",
        )

        assertEquals(75.5, subject.effectiveAmount, 0.001)
    }

    @Test
    fun `effectiveAmount — manual mode with unparseable text — falls back to zero`() {
        val subject = AgregarListaNegraUiState(totalMode = TotalMode.MANUAL, manualAmount = "abc")

        assertEquals(0.0, subject.effectiveAmount, 0.001)
    }

    @Test
    fun `effectiveAmount — manual mode with an empty amount — falls back to zero`() {
        val subject = AgregarListaNegraUiState(totalMode = TotalMode.MANUAL, manualAmount = "")

        assertEquals(0.0, subject.effectiveAmount, 0.001)
    }

    @Test
    fun `canConfirm — reason is only whitespace — is false`() {
        val subject = AgregarListaNegraUiState(
            totalMode = TotalMode.MANUAL,
            manualAmount = "50",
            reason = "   ",
        )

        assertFalse(subject.canConfirm)
    }

    @Test
    fun `canConfirm — amount is zero — is false`() {
        val subject = AgregarListaNegraUiState(
            totalMode = TotalMode.MANUAL,
            manualAmount = "0",
            reason = "No paga",
        )

        assertFalse(subject.canConfirm)
    }

    @Test
    fun `canConfirm — reason and amount both present — is true`() {
        val subject = AgregarListaNegraUiState(
            totalMode = TotalMode.MANUAL,
            manualAmount = "50",
            reason = "No paga",
        )

        assertTrue(subject.canConfirm)
    }

    @Test
    fun `canConfirm — default state — is false`() {
        assertFalse(AgregarListaNegraUiState().canConfirm)
    }
}
