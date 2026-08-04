package com.restrusher.ecomercecarlosv.data.local.dao

import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.fixtures.detallePedidoEntity
import com.restrusher.ecomercecarlosv.fixtures.pedidoEntity
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import com.restrusher.ecomercecarlosv.support.seedMercadoAndCliente
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PedidoDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PedidoDao

    @Before
    fun setUp() = runTest {
        db = createTestDatabase()
        dao = db.pedidoDao()
        db.seedMercadoAndCliente()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `insert then getById — round-trips the row`() = runTest {
        dao.insert(pedidoEntity(id = "p1"))

        assertEquals("p1", dao.getById("p1")?.id)
    }

    @Test
    fun `insert — ignores a duplicate id rather than replacing the row`() = runTest {
        dao.insert(pedidoEntity(id = "p1", status = "PENDING"))
        dao.insert(pedidoEntity(id = "p1", status = "PAID"))

        assertEquals("PENDING", dao.getById("p1")?.status)
    }

    @Test
    fun `getById — a soft-deleted pedido is invisible`() = runTest {
        dao.insert(pedidoEntity(id = "p1"))

        dao.softDeleteById("p1")

        assertNull(dao.getById("p1"))
    }

    @Test
    fun `softDelete — keeps the row so the deletion can still be pushed`() = runTest {
        dao.insert(pedidoEntity(id = "p1"))

        dao.softDeleteById("p1")

        // Invisible to every query, but still on disk until the queue confirms the remote delete.
        assertEquals(1, dao.countByCreatedAtBefore(Long.MAX_VALUE))
    }

    @Test
    fun `getAll — excludes soft-deleted rows and sorts newest first`() = runTest {
        dao.insert(pedidoEntity(id = "viejo").copy(createdAt = 1_000L))
        dao.insert(pedidoEntity(id = "nuevo").copy(createdAt = 9_000L))
        dao.insert(pedidoEntity(id = "borrado").copy(createdAt = 5_000L))
        dao.softDeleteById("borrado")

        dao.getAll().test {
            assertEquals(listOf("nuevo", "viejo"), awaitItem().map { it.id })
        }
    }

    @Test
    fun `getAllUnpaid — excludes PAID and soft-deleted rows`() = runTest {
        dao.insert(pedidoEntity(id = "pendiente", status = "PENDING"))
        dao.insert(pedidoEntity(id = "parcial", status = "PARTIAL"))
        dao.insert(pedidoEntity(id = "pagado", status = "PAID"))
        dao.insert(pedidoEntity(id = "borrado", status = "PENDING"))
        dao.softDeleteById("borrado")

        dao.getAllUnpaid().test {
            assertEquals(setOf("pendiente", "parcial"), awaitItem().map { it.id }.toSet())
        }
    }

    @Test
    fun `getByCliente — only that cliente's pedidos`() = runTest {
        db.seedMercadoAndCliente(mercadoId = "mercado-2", clienteId = "cliente-2")
        dao.insert(pedidoEntity(id = "p1").copy(clienteId = "cliente-1"))
        dao.insert(pedidoEntity(id = "p2").copy(clienteId = "cliente-2"))

        dao.getByCliente("cliente-1").test {
            assertEquals(listOf("p1"), awaitItem().map { it.id })
        }
    }

    @Test
    fun `a flow re-emits when a row is written`() = runTest {
        dao.getAll().test {
            assertEquals(emptyList<String>(), awaitItem().map { it.id })

            dao.insert(pedidoEntity(id = "p1"))

            assertEquals(listOf("p1"), awaitItem().map { it.id })
        }
    }

    @Test
    fun `getByClienteWithLines — attaches the detalle rows to their pedido`() = runTest {
        dao.insert(pedidoEntity(id = "p1"))
        db.detallePedidoDao().insertAll(
            listOf(
                detallePedidoEntity(id = "d1", pedidoId = "p1", productName = "Arroz"),
                detallePedidoEntity(id = "d2", pedidoId = "p1", productName = "Azúcar"),
            ),
        )

        dao.getByClienteWithLines("cliente-1").test {
            val withLines = awaitItem().single()

            assertEquals("p1", withLines.pedido.id)
            assertEquals(setOf("Arroz", "Azúcar"), withLines.lines.map { it.productName }.toSet())
        }
    }

    @Test
    fun `markAllPaidForCliente — settles every unpaid pedido and leaves paid ones alone`() = runTest {
        dao.insert(pedidoEntity(id = "p1", status = "PARTIAL").copy(total = 100.0, paid = 40.0))
        dao.insert(pedidoEntity(id = "p2", status = "PENDING").copy(total = 50.0, paid = 0.0))
        dao.insert(pedidoEntity(id = "p3", status = "PAID").copy(total = 20.0, paid = 20.0, paidAt = 1L))

        dao.markAllPaidForCliente("cliente-1", paidAt = 7_777L)

        assertEquals(100.0, dao.getById("p1")!!.paid, 0.001)
        assertEquals(50.0, dao.getById("p2")!!.paid, 0.001)
        assertEquals(7_777L, dao.getById("p1")!!.paidAt)
        // The already-paid pedido keeps its original timestamp.
        assertEquals(1L, dao.getById("p3")!!.paidAt)
    }

    @Test
    fun `updateAfterEdit — rewrites the totals, count and status in one statement`() = runTest {
        dao.insert(pedidoEntity(id = "p1", status = "PENDING", itemCount = 1))

        dao.updateAfterEdit("p1", total = 250.0, itemCount = 4, status = "PARTIAL", paid = 60.0, paidAt = 42L)

        val updated = dao.getById("p1")!!
        assertEquals(250.0, updated.total, 0.001)
        assertEquals(4, updated.itemCount)
        assertEquals("PARTIAL", updated.status)
        assertEquals(60.0, updated.paid, 0.001)
        assertEquals(42L, updated.paidAt)
    }

    @Test
    fun `deleting a pedido cascades to its detalle rows`() = runTest {
        dao.insert(pedidoEntity(id = "p1"))
        db.detallePedidoDao().insertAll(listOf(detallePedidoEntity(id = "d1", pedidoId = "p1")))

        dao.deleteById("p1")

        assertEquals(emptyList<Any>(), db.detallePedidoDao().getByPedido("p1"))
    }
}
