package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.fixtures.cliente
import com.restrusher.ecomercecarlosv.fixtures.mercadoEntity
import com.restrusher.ecomercecarlosv.support.createTestDatabase
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

@RunWith(RobolectricTestRunner::class)
class ClienteRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ClienteRepositoryImpl

    @Before
    fun setUp() = runTest {
        db = createTestDatabase()
        db.mercadoDao().insert(mercadoEntity(id = "mercado-1"))
        repository = ClienteRepositoryImpl(
            dao = db.clienteDao(),
            syncOperationDao = db.syncOperationDao(),
            dataSynchronizer = mockk<DataSynchronizer>(relaxed = true),
        )
    }

    @After
    fun tearDown() = db.close()

    private suspend fun queue() = db.syncOperationDao().getPending()

    @Test
    fun `save — inserts a new cliente and queues an upsert`() = runTest {
        repository.save(cliente(id = "c1", name = "Ana"))

        assertEquals("Ana", repository.getById("c1")?.name)
        val queued = queue().single()
        assertEquals(EntityType.CLIENTE, queued.entityType)
        assertEquals(SyncOp.UPSERT, queued.operation)
        assertEquals("Ana", queued.entityLabel)
    }

    @Test
    fun `save — a second save updates in place instead of being ignored`() = runTest {
        repository.save(cliente(id = "c1", name = "Ana"))

        repository.save(cliente(id = "c1", name = "Ana María"))

        assertEquals("Ana María", repository.getById("c1")?.name)
        assertEquals(2, queue().size)
    }

    @Test
    fun `save — phones survive the trip through the entity`() = runTest {
        repository.save(cliente(id = "c1", name = "Ana"))

        assertEquals(listOf("70000001"), repository.getById("c1")?.phones)
    }

    @Test
    fun `blacklist — records the details and queues an upsert labelled with the name`() = runTest {
        repository.save(cliente(id = "c1", name = "Ana"))

        repository.blacklist("c1", reason = "No paga", balance = 250.0, at = 7_777L, isManualAmount = true)

        val stored = repository.getById("c1")!!
        assertTrue(stored.isBlacklisted)
        assertEquals(250.0, stored.blacklistBalance, 0.001)
        assertTrue(stored.blacklistIsManualAmount)
        assertEquals("Ana", queue().last().entityLabel)
    }

    @Test
    fun `unblacklist — clears the flags and queues an upsert, not a delete`() = runTest {
        repository.save(cliente(id = "c1", name = "Ana"))
        repository.blacklist("c1", reason = "No paga", balance = 250.0, at = 7_777L, isManualAmount = false)

        repository.unblacklist("c1")

        assertEquals(false, repository.getById("c1")?.isBlacklisted)
        assertEquals(SyncOp.UPSERT, queue().last().operation)
    }

    @Test
    fun `delete — soft-deletes locally and queues a DELETE`() = runTest {
        repository.save(cliente(id = "c1", name = "Ana"))

        repository.delete("c1")

        assertNull(repository.getById("c1"))
        val queued = queue().last()
        assertEquals(SyncOp.DELETE, queued.operation)
        assertEquals("Ana", queued.entityLabel)
    }

    @Test
    fun `delete — an unknown id still queues, with an empty label`() = runTest {
        repository.delete("no-existe")

        assertEquals("", queue().single().entityLabel)
    }
}
