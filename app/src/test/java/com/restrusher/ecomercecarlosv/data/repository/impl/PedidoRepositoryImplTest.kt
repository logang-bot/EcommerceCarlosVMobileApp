package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.Pago
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.fixtures.detallePedido
import com.restrusher.ecomercecarlosv.fixtures.pedido
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import com.restrusher.ecomercecarlosv.support.seedMercadoAndCliente
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Real Room, mocked synchronizer. Every mutating method is expected to write locally **and** leave
 * a `sync_operations` row behind — that pairing is the whole offline-first contract.
 */
@RunWith(RobolectricTestRunner::class)
class PedidoRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: PedidoRepositoryImpl

    @Before
    fun setUp() = runTest {
        db = createTestDatabase()
        db.seedMercadoAndCliente()
        repository = PedidoRepositoryImpl(
            pedidoDao = db.pedidoDao(),
            detallePedidoDao = db.detallePedidoDao(),
            pagoDao = db.pagoDao(),
            syncOperationDao = db.syncOperationDao(),
            dataSynchronizer = mockk<DataSynchronizer>(relaxed = true),
        )
    }

    @After
    fun tearDown() = db.close()

    private suspend fun queue() = db.syncOperationDao().getPending()

    private suspend fun pagosOf(pedidoId: String) = db.pagoDao().getByPedido(pedidoId)

    @Test
    fun `create — writes the pedido, its lines, and one queued upsert`() = runTest {
        val nuevo = pedido(id = "p1", total = 100.0, paid = 0.0)

        repository.create(nuevo, listOf(detallePedido(id = "d1", pedidoId = "p1")))

        assertEquals("p1", db.pedidoDao().getById("p1")?.id)
        assertEquals(1, db.detallePedidoDao().getByPedido("p1").size)
        val queued = queue().single()
        assertEquals(EntityType.PEDIDO, queued.entityType)
        assertEquals(SyncOp.UPSERT, queued.operation)
        assertEquals("p1", queued.entityId)
    }

    @Test
    fun `create — an unpaid pedido records no pago row`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 0.0), emptyList())

        assertEquals(emptyList<Any>(), pagosOf("p1"))
    }

    @Test
    fun `create — an initial payment is recorded as the first pago`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 40.0, createdAt = 5_000L), emptyList())

        val pago = pagosOf("p1").single()
        assertEquals(40.0, pago.amount, 0.001)
        assertEquals(5_000L, pago.paidAt)
    }

    @Test
    fun `create — a fully paid pedido stamps the pago with paidAt, not createdAt`() = runTest {
        val pagado = pedido(
            id = "p1",
            status = PedidoStatus.PAID,
            total = 100.0,
            paid = 100.0,
            createdAt = 5_000L,
            paidAt = 8_000L,
        )

        repository.create(pagado, emptyList())

        assertEquals(8_000L, pagosOf("p1").single().paidAt)
    }

    @Test
    fun `updateLines — recomputes the status from the new total`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 100.0, status = PedidoStatus.PAID), emptyList())

        // The order grew, so the same money no longer covers it.
        repository.updateLines("p1", emptyList(), newTotal = 250.0, paid = 100.0, paidAt = null)

        assertEquals("PARTIAL", db.pedidoDao().getById("p1")?.status)
    }

    @Test
    fun `updateLines — shrinking the order below what was paid settles it`() = runTest {
        repository.create(pedido(id = "p1", total = 200.0, paid = 100.0, status = PedidoStatus.PARTIAL), emptyList())

        repository.updateLines("p1", emptyList(), newTotal = 80.0, paid = 100.0, paidAt = 9_000L)

        assertEquals("PAID", db.pedidoDao().getById("p1")?.status)
    }

    @Test
    fun `updateLines — no extra payment means no new pago row`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 40.0), emptyList())

        repository.updateLines("p1", emptyList(), newTotal = 250.0, paid = 40.0, paidAt = null)

        assertEquals(1, pagosOf("p1").size)
    }

    @Test
    fun `updateLines — only the delta is recorded when more money is handed over`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 40.0), emptyList())

        repository.updateLines("p1", emptyList(), newTotal = 250.0, paid = 100.0, paidAt = 9_000L)

        val delta = pagosOf("p1").last()
        assertEquals(60.0, delta.amount, 0.001)
        assertEquals(9_000L, delta.paidAt)
    }

    @Test
    fun `updateLines — a reduced payment records nothing rather than a negative pago`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 80.0), emptyList())

        repository.updateLines("p1", emptyList(), newTotal = 100.0, paid = 30.0, paidAt = null)

        assertEquals(1, pagosOf("p1").size)
        assertTrue(pagosOf("p1").none { it.amount < 0 })
    }

    @Test
    fun `updateLines — replaces the previous lines rather than appending`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0), listOf(detallePedido(id = "d1", pedidoId = "p1")))

        repository.updateLines(
            "p1",
            listOf(detallePedido(id = "d2", pedidoId = "p1"), detallePedido(id = "d3", pedidoId = "p1")),
            newTotal = 200.0,
            paid = 0.0,
            paidAt = null,
        )

        val lines = db.detallePedidoDao().getByPedido("p1")
        assertEquals(setOf("d2", "d3"), lines.map { it.id }.toSet())
        assertEquals(2, db.pedidoDao().getById("p1")?.itemCount)
    }

    @Test
    fun `registrarPago — writes the pago and moves the pedido to its new status`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 0.0), emptyList())

        repository.registrarPago(
            Pago(id = "pago-1", pedidoId = "p1", amount = 100.0, paidAt = 4_242L),
            newPaid = 100.0,
            newStatus = PedidoStatus.PAID,
        )

        val updated = db.pedidoDao().getById("p1")!!
        assertEquals("PAID", updated.status)
        assertEquals(100.0, updated.paid, 0.001)
        assertEquals(4_242L, updated.paidAt)
        assertEquals(1, pagosOf("p1").size)
    }

    @Test
    fun `delete — soft-deletes locally and queues a DELETE for the server`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0), emptyList())

        repository.delete("p1")

        assertNull(repository.getById("p1"))
        assertEquals(SyncOp.DELETE, queue().last().operation)
    }

    @Test
    fun `every mutating call leaves exactly one queued operation`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0), emptyList())
        assertEquals(1, queue().size)

        repository.updateStatus("p1", PedidoStatus.PARTIAL, paid = 10.0, paidAt = null)
        assertEquals(2, queue().size)

        repository.updateDate("p1", createdAt = 123L)
        assertEquals(3, queue().size)

        repository.updateLines("p1", emptyList(), newTotal = 50.0, paid = 10.0, paidAt = null)
        assertEquals(4, queue().size)
    }

    @Test
    fun `markAllPaidForCliente — settles the pedidos but queues nothing to sync`() = runTest {
        repository.create(pedido(id = "p1", total = 100.0, paid = 0.0), emptyList())
        db.syncOperationDao().delete(queue().single().id)

        repository.markAllPaidForCliente("cliente-1")

        assertEquals("PAID", db.pedidoDao().getById("p1")?.status)
        // Documents current behaviour: this is the one mutating method that does not enqueue, so
        // the settlement stays on the device. See docs/features/testing.md.
        assertEquals(emptyList<Any>(), queue())
    }
}
