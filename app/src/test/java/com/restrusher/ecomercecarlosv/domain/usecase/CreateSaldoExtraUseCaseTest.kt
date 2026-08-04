package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateSaldoExtraUseCaseTest {

    private val repository = FakePedidoRepository()
    private val useCase = CreateSaldoExtraUseCase(repository)

    private val created get() = repository.created.single()

    private suspend fun crear(amount: Double = 80.0, date: Long = 1_700_000_000_000L) =
        useCase(clienteId = "cliente-1", description = "Préstamo", amount = amount, date = date)

    @Test
    fun `invoke — is flagged as saldo extra so it is excluded from the status calculation`() = runTest {
        crear()

        assertTrue(created.pedido.isSaldoExtra)
    }

    @Test
    fun `invoke — is always PENDING and unpaid, whatever the amount`() = runTest {
        crear(amount = 80.0)

        assertEquals(PedidoStatus.PENDING, created.pedido.status)
        assertEquals(0.0, created.pedido.paid, 0.001)
        assertNull(created.pedido.paidAt)
    }

    @Test
    fun `invoke — the amount becomes the total, so the whole sum is pending`() = runTest {
        crear(amount = 80.0)

        assertEquals(80.0, created.pedido.total, 0.001)
        assertEquals(80.0, created.pedido.pending, 0.001)
    }

    @Test
    fun `invoke — the description is stored as the pedido notes`() = runTest {
        crear()

        assertEquals("Préstamo", created.pedido.notes)
    }

    @Test
    fun `invoke — uses the caller's date rather than the current time`() = runTest {
        crear(date = 1_600_000_000_000L)

        assertEquals(1_600_000_000_000L, created.pedido.createdAt)
    }

    @Test
    fun `invoke — has no line items and reports no item count`() = runTest {
        crear()

        assertEquals(emptyList<Any>(), created.detalles)
        assertEquals(0, created.pedido.itemCount)
    }

    @Test
    fun `invoke — returns the id of the pedido it persisted`() = runTest {
        val pedidoId = crear()

        assertEquals(pedidoId, created.pedido.id)
        assertEquals("cliente-1", created.pedido.clienteId)
    }
}
