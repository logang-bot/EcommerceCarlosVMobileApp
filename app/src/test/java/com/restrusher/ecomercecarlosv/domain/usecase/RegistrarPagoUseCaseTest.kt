package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import com.restrusher.ecomercecarlosv.fixtures.pedido
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrarPagoUseCaseTest {

    private val repository = FakePedidoRepository()
    private val useCase = RegistrarPagoUseCase(repository)

    private val registered get() = repository.registeredPagos.single()

    @Test
    fun `newPaid — adds the amount to what was already paid`() = runTest {
        useCase(pedido(total = 100.0, paid = 30.0), amount = 20.0)

        assertEquals(50.0, registered.newPaid, 0.001)
    }

    @Test
    fun `status — still short of the total — is PARTIAL`() = runTest {
        useCase(pedido(total = 100.0, paid = 30.0), amount = 20.0)

        assertEquals(PedidoStatus.PARTIAL, registered.newStatus)
    }

    @Test
    fun `status — payment settles the balance exactly — is PAID`() = runTest {
        useCase(pedido(total = 100.0, paid = 30.0), amount = 70.0)

        assertEquals(PedidoStatus.PAID, registered.newStatus)
        assertEquals(100.0, registered.newPaid, 0.001)
    }

    @Test
    fun `newPaid — overpayment is clamped to the total`() = runTest {
        useCase(pedido(total = 100.0, paid = 30.0), amount = 500.0)

        assertEquals(100.0, registered.newPaid, 0.001)
        assertEquals(PedidoStatus.PAID, registered.newStatus)
    }

    @Test
    fun `pago — records the amount the user actually handed over, not the clamped balance`() = runTest {
        useCase(pedido(total = 100.0, paid = 30.0), amount = 500.0)

        assertEquals(500.0, registered.pago.amount, 0.001)
    }

    @Test
    fun `pago — points at the pedido being paid and is stamped with a payment time`() = runTest {
        useCase(pedido(id = "pedido-9", total = 100.0, paid = 0.0), amount = 10.0)

        assertEquals("pedido-9", registered.pago.pedidoId)
        assertTrue(registered.pago.paidAt > 0L)
    }

    @Test
    fun `status — paying off a pedido whose balance is already zero — is PAID`() = runTest {
        useCase(pedido(total = 100.0, paid = 100.0), amount = 0.0)

        assertEquals(PedidoStatus.PAID, registered.newStatus)
    }

    @Test
    fun `status — a zero-total saldo extra — is PAID because zero already covers it`() = runTest {
        useCase(pedido(total = 0.0, paid = 0.0, isSaldoExtra = true), amount = 0.0)

        assertEquals(PedidoStatus.PAID, registered.newStatus)
    }

    @Test
    fun `marcar pagado — paying exactly the pending balance settles the pedido`() = runTest {
        val subject = pedido(total = 100.0, paid = 30.0)

        useCase(subject, amount = subject.pending)

        assertEquals(PedidoStatus.PAID, registered.newStatus)
        assertEquals(100.0, registered.newPaid, 0.001)
    }

    @Test
    fun `pago — each payment gets its own id`() = runTest {
        val subject = pedido(total = 100.0, paid = 0.0)

        useCase(subject, amount = 10.0)
        useCase(subject, amount = 10.0)

        val ids = repository.registeredPagos.map { it.pago.id }
        assertEquals(2, ids.distinct().size)
    }
}
