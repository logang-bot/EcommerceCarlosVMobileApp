package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import com.restrusher.ecomercecarlosv.fixtures.cartItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePedidoUseCaseTest {

    private val repository = FakePedidoRepository()
    private val useCase = CreatePedidoUseCase(repository)

    private val dosItems = listOf(
        cartItem(productoId = "p1", unitPrice = 12.5, quantity = 2),
        cartItem(productoId = "p2", unitPrice = 30.0, quantity = 1),
    )

    private val createdPedido get() = repository.created.single().pedido

    @Test
    fun `total — is the sum of unit price times quantity across every item`() = runTest {
        useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 0.0)

        assertEquals(55.0, createdPedido.total, 0.001)
    }

    @Test
    fun `status — no initial payment — is PENDING with no paid timestamp`() = runTest {
        useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 0.0)

        assertEquals(PedidoStatus.PENDING, createdPedido.status)
        assertNull(createdPedido.paidAt)
    }

    @Test
    fun `status — payment below the total — is PARTIAL with no paid timestamp`() = runTest {
        useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 20.0)

        assertEquals(PedidoStatus.PARTIAL, createdPedido.status)
        assertNull(createdPedido.paidAt)
    }

    @Test
    fun `status — payment covering the total exactly — is PAID and stamps paidAt`() = runTest {
        useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 55.0)

        assertEquals(PedidoStatus.PAID, createdPedido.status)
        assertNotNull(createdPedido.paidAt)
        assertEquals(createdPedido.createdAt, createdPedido.paidAt)
    }

    @Test
    fun `paid — overpayment is stored unclamped, unlike RegistrarPagoUseCase which coerces`() = runTest {
        useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 80.0)

        assertEquals(PedidoStatus.PAID, createdPedido.status)
        assertEquals(80.0, createdPedido.paid, 0.001)
        assertEquals(0.0, createdPedido.pending, 0.001)
    }

    @Test
    fun `status — an empty cart with no payment — is PAID because zero covers a zero total`() = runTest {
        useCase(clienteId = "cliente-1", items = emptyList(), initialPayment = 0.0)

        assertEquals(0.0, createdPedido.total, 0.001)
        assertEquals(PedidoStatus.PAID, createdPedido.status)
    }

    @Test
    fun `itemCount — counts cart lines, not units`() = runTest {
        val items = listOf(cartItem(productoId = "p1", quantity = 7), cartItem(productoId = "p2", quantity = 3))

        useCase(clienteId = "cliente-1", items = items, initialPayment = 0.0)

        assertEquals(2, createdPedido.itemCount)
    }

    @Test
    fun `detalles — one line per cart item, all pointing at the new pedido`() = runTest {
        val pedidoId = useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 0.0)

        val detalles = repository.created.single().detalles
        assertEquals(2, detalles.size)
        assertTrue(detalles.all { it.pedidoId == pedidoId })
        assertEquals(listOf("p1", "p2"), detalles.map { it.productoId })
    }

    @Test
    fun `detalles — carry the sold price and the catalog price separately`() = runTest {
        val items = listOf(cartItem(unitPrice = 8.0, catalogPrice = 10.0, quantity = 1))

        useCase(clienteId = "cliente-1", items = items, initialPayment = 0.0)

        val detalle = repository.created.single().detalles.single()
        assertEquals(8.0, detalle.unitPrice, 0.001)
        assertEquals(10.0, detalle.catalogPrice, 0.001)
        assertTrue(detalle.isPriceModified)
    }

    @Test
    fun `detalles — each line gets its own id, distinct from the pedido id`() = runTest {
        val pedidoId = useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 0.0)

        val ids = repository.created.single().detalles.map { it.id }
        assertEquals(2, ids.distinct().size)
        assertTrue(ids.none { it == pedidoId })
    }

    @Test
    fun `invoke — returns the id of the pedido it persisted`() = runTest {
        val pedidoId = useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 0.0)

        assertEquals(pedidoId, createdPedido.id)
        assertEquals("cliente-1", createdPedido.clienteId)
    }

    @Test
    fun `invoke — a pedido from the cart is never flagged as saldo extra`() = runTest {
        useCase(clienteId = "cliente-1", items = dosItems, initialPayment = 0.0)

        assertEquals(false, createdPedido.isSaldoExtra)
    }
}
