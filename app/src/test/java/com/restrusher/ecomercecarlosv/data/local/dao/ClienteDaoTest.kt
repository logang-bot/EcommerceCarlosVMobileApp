package com.restrusher.ecomercecarlosv.data.local.dao

import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.fixtures.clienteEntity
import com.restrusher.ecomercecarlosv.fixtures.mercadoEntity
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClienteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ClienteDao

    @Before
    fun setUp() = runTest {
        db = createTestDatabase()
        dao = db.clienteDao()
        db.mercadoDao().insert(mercadoEntity(id = "mercado-1"))
    }

    @After
    fun tearDown() = db.close()

    private fun activo(id: String, name: String, mercadoId: String = "mercado-1") =
        clienteEntity(id = id).copy(name = name, mercadoId = mercadoId, isBlacklisted = false)

    @Test
    fun `insert returns -1 for a duplicate id, which is what drives the upsert`() = runTest {
        assertTrue(dao.insert(activo("c1", "Ana")) > 0)

        assertEquals(-1L, dao.insert(activo("c1", "Ana otra vez")))
    }

    @Test
    fun `update — overwrites a row the insert refused to replace`() = runTest {
        dao.insert(activo("c1", "Ana"))

        dao.update(activo("c1", "Ana María"))

        assertEquals("Ana María", dao.getById("c1")?.name)
    }

    @Test
    fun `getAll — hides blacklisted clientes and sorts by name`() = runTest {
        dao.insert(activo("c1", "Zulema"))
        dao.insert(activo("c2", "Ana"))
        dao.insert(clienteEntity(id = "c3").copy(name = "Beto", isBlacklisted = true))

        dao.getAll().test {
            assertEquals(listOf("Ana", "Zulema"), awaitItem().map { it.name })
        }
    }

    @Test
    fun `getAllIncludingBlacklisted — keeps them in the list`() = runTest {
        dao.insert(activo("c1", "Ana"))
        dao.insert(clienteEntity(id = "c2").copy(name = "Beto", isBlacklisted = true))

        dao.getAllIncludingBlacklisted().test {
            assertEquals(listOf("Ana", "Beto"), awaitItem().map { it.name })
        }
    }

    @Test
    fun `getBlacklisted — newest blacklisting first`() = runTest {
        dao.insert(clienteEntity(id = "c1").copy(name = "Ana", isBlacklisted = true, blacklistedAt = 1_000L))
        dao.insert(clienteEntity(id = "c2").copy(name = "Beto", isBlacklisted = true, blacklistedAt = 9_000L))

        dao.getBlacklisted().test {
            assertEquals(listOf("Beto", "Ana"), awaitItem().map { it.name })
        }
    }

    @Test
    fun `getByMercado — only that mercado's active clientes`() = runTest {
        db.mercadoDao().insert(mercadoEntity(id = "mercado-2"))
        dao.insert(activo("c1", "Ana", mercadoId = "mercado-1"))
        dao.insert(activo("c2", "Beto", mercadoId = "mercado-2"))

        dao.getByMercado("mercado-1").test {
            assertEquals(listOf("Ana"), awaitItem().map { it.name })
        }
    }

    @Test
    fun `blacklist — records the reason, balance and manual flag together`() = runTest {
        dao.insert(activo("c1", "Ana"))

        dao.blacklist("c1", reason = "No paga", balance = 250.0, at = 7_777L, isManualAmount = true)

        val cliente = dao.getById("c1")!!
        assertTrue(cliente.isBlacklisted)
        assertEquals("No paga", cliente.blacklistReason)
        assertEquals(250.0, cliente.blacklistBalance, 0.001)
        assertEquals(7_777L, cliente.blacklistedAt)
        assertTrue(cliente.blacklistIsManualAmount)
    }

    @Test
    fun `unblacklist — clears every blacklist field, not just the flag`() = runTest {
        dao.insert(activo("c1", "Ana"))
        dao.blacklist("c1", reason = "No paga", balance = 250.0, at = 7_777L, isManualAmount = true)

        dao.unblacklist("c1")

        val cliente = dao.getById("c1")!!
        assertFalse(cliente.isBlacklisted)
        assertNull(cliente.blacklistReason)
        assertNull(cliente.blacklistedAt)
        assertEquals(0.0, cliente.blacklistBalance, 0.001)
        assertFalse(cliente.blacklistIsManualAmount)
    }

    @Test
    fun `softDelete — hides the cliente from every read`() = runTest {
        dao.insert(activo("c1", "Ana"))

        dao.softDeleteById("c1")

        assertNull(dao.getById("c1"))
        dao.getAllIncludingBlacklisted().test {
            assertEquals(emptyList<String>(), awaitItem().map { it.id })
        }
    }

    @Test
    fun `deleting a mercado cascades to its clientes`() = runTest {
        dao.insert(activo("c1", "Ana"))

        db.mercadoDao().deleteById("mercado-1")

        assertNull(dao.getById("c1"))
    }
}
