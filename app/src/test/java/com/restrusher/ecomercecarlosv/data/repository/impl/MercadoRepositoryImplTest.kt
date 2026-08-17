package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.fixtures.clienteEntity
import com.restrusher.ecomercecarlosv.fixtures.mercadoEntity
import com.restrusher.ecomercecarlosv.fixtures.pedidoEntity
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MercadoRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: MercadoRepositoryImpl

    @Before
    fun setUp() = runTest {
        db = createTestDatabase()
        repository = MercadoRepositoryImpl(
            dao = db.mercadoDao(),
            clienteDao = db.clienteDao(),
            pedidoDao = db.pedidoDao(),
            syncOperationDao = db.syncOperationDao(),
            dataSynchronizer = mockk<DataSynchronizer>(relaxed = true),
        )
        db.mercadoDao().insert(mercadoEntity(id = "mercado-1"))
        db.clienteDao().insert(clienteEntity(id = "cliente-1").copy(mercadoId = "mercado-1"))
        db.pedidoDao().insert(pedidoEntity(id = "p1"))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `delete — soft-deletes the whole subtree locally`() = runTest {
        repository.delete("mercado-1")

        assertNull(repository.getById("mercado-1"))
        assertNull(db.clienteDao().getById("cliente-1"))
        assertNull(db.pedidoDao().getById("p1"))
    }

    /**
     * The Supabase cascade trigger propagates to the children, so queuing an op per child would be
     * redundant and would flood `sync_operations` for a large mercado. Guards that decision.
     */
    @Test
    fun `delete — queues exactly one op, for the mercado itself`() = runTest {
        repository.delete("mercado-1")

        val queued = db.syncOperationDao().getPending().single()
        assertEquals(EntityType.MERCADO, queued.entityType)
        assertEquals(SyncOp.DELETE, queued.operation)
        assertEquals("mercado-1", queued.entityId)
    }

    @Test
    fun `delete — leaves another mercado's subtree untouched`() = runTest {
        db.mercadoDao().insert(mercadoEntity(id = "mercado-2"))
        db.clienteDao().insert(clienteEntity(id = "cliente-2").copy(mercadoId = "mercado-2"))
        db.pedidoDao().insert(pedidoEntity(id = "p2").copy(clienteId = "cliente-2"))

        repository.delete("mercado-1")

        assertEquals("mercado-2", repository.getById("mercado-2")?.id)
        assertEquals("cliente-2", db.clienteDao().getById("cliente-2")?.id)
        assertEquals("p2", db.pedidoDao().getById("p2")?.id)
    }

    @Test
    fun `save — inserts and queues an upsert`() = runTest {
        val nuevo = repository.getById("mercado-1")!!.copy(id = "mercado-3", name = "Mercado Nuevo")

        repository.save(nuevo)

        assertEquals("Mercado Nuevo", repository.getById("mercado-3")?.name)
        assertEquals(SyncOp.UPSERT, db.syncOperationDao().getPending().single().operation)
    }
}
