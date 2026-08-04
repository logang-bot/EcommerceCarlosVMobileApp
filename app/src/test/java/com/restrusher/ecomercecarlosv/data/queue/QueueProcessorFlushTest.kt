package com.restrusher.ecomercecarlosv.data.queue

import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.remote.StorageService
import com.restrusher.ecomercecarlosv.domain.error.AppError
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fixtures.mercadoEntity
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import com.restrusher.ecomercecarlosv.support.queueProcessorOver
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The push loop itself.
 *
 * Operations are queued for entities that no longer exist in Room. `upsert()` returns early on a
 * missing row (`?: return`), so these exercise dedup, queue bookkeeping and outcome reporting
 * without ever reaching Supabase — whose `from()` is an extension function that cannot be stubbed.
 * Failures are produced through a throwing [StorageService] instead, which is the one failure path
 * that also stops short of the network.
 */
@RunWith(RobolectricTestRunner::class)
class QueueProcessorFlushTest {

    private lateinit var db: AppDatabase
    private val session = FakeSessionManager()
    private val errorHandler = GlobalErrorHandler()

    @Before
    fun setUp() {
        db = createTestDatabase()
    }

    @After
    fun tearDown() = db.close()

    private fun processor(scope: TestScope, storageService: StorageService = mockk(relaxed = true)) =
        queueProcessorOver(
            db,
            scope,
            sessionManager = session,
            storageService = storageService,
            errorHandler = errorHandler,
        )

    private suspend fun enqueue(
        entityId: String,
        operation: String = SyncOp.UPSERT,
        entityType: String = EntityType.MERCADO,
        createdAt: Long,
    ) = db.syncOperationDao().enqueue(
        SyncOperationEntity(
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            createdAt = createdAt,
        ),
    )

    @Test
    fun `repeated upserts for one entity collapse, and every matching row leaves the queue`() =
        runTest {
            enqueue("mercado-1", createdAt = 1_000L)
            enqueue("mercado-1", createdAt = 2_000L)
            enqueue("mercado-1", createdAt = 3_000L)

            val outcome = processor(this).flush()

            assertEquals(FlushOutcome.COMPLETED, outcome)
            assertEquals(0, db.syncOperationDao().pendingCount())
        }

    @Test
    fun `operations for different entities are not collapsed together`() = runTest {
        enqueue("mercado-1", createdAt = 1_000L)
        enqueue("mercado-2", createdAt = 2_000L)

        processor(this).flush()

        assertEquals(0, db.syncOperationDao().pendingCount())
    }

    @Test
    fun `the same id under a different entity type is a different entity`() = runTest {
        enqueue("shared-id", entityType = EntityType.MERCADO, createdAt = 1_000L)
        enqueue("shared-id", entityType = EntityType.CLIENTE, createdAt = 2_000L)

        assertEquals(FlushOutcome.COMPLETED, processor(this).flush())
        assertEquals(0, db.syncOperationDao().pendingCount())
    }

    @Test
    fun `an unknown operation name is skipped rather than failing the flush`() = runTest {
        enqueue("mercado-1", operation = "FRANKENSTEIN", createdAt = 1_000L)

        assertEquals(FlushOutcome.COMPLETED, processor(this).flush())
        assertEquals(0, db.syncOperationDao().pendingCount())
    }

    @Test
    fun `an unknown entity type is skipped rather than failing the flush`() = runTest {
        enqueue("x", entityType = "MARCIANO", createdAt = 1_000L)

        assertEquals(FlushOutcome.COMPLETED, processor(this).flush())
    }

    @Test
    fun `a successful flush stamps lastSuccessfulFlushAt`() = runTest {
        val processor = processor(this)
        assertNull(processor.lastSuccessfulFlushAt.value)
        enqueue("mercado-1", createdAt = 1_000L)

        processor.flush()

        assertNotNull(processor.lastSuccessfulFlushAt.value)
    }

    @Test
    fun `an empty queue does not stamp lastSuccessfulFlushAt`() = runTest {
        val processor = processor(this)

        processor.flush()

        // pushPending returns before reporting, so "nothing to do" is not "just synced".
        assertNull(processor.lastSuccessfulFlushAt.value)
    }

    @Test
    fun `a rejected operation reports FAILED, keeps the row and increments its retry count`() =
        runTest {
            db.mercadoDao().insert(mercadoEntity(id = "mercado-1").copy(photoUrl = "content://foto"))
            enqueue("mercado-1", createdAt = 1_000L)
            val storage = mockk<StorageService> {
                coEvery { uploadPhoto(any(), any(), any()) } throws IllegalStateException("upload rechazado")
            }

            val outcome = processor(this, storage).flush()

            assertEquals(FlushOutcome.FAILED, outcome)
            assertEquals(listOf(1), db.syncOperationDao().getPending().map { it.retryCount })
        }

    @Test
    fun `a failure increments every queued row for that entity, not just the deduplicated one`() =
        runTest {
            db.mercadoDao().insert(mercadoEntity(id = "mercado-1").copy(photoUrl = "content://foto"))
            enqueue("mercado-1", createdAt = 1_000L)
            enqueue("mercado-1", createdAt = 2_000L)
            val storage = mockk<StorageService> {
                coEvery { uploadPhoto(any(), any(), any()) } throws IllegalStateException("upload rechazado")
            }

            processor(this, storage).flush()

            assertEquals(listOf(1, 1), db.syncOperationDao().getPending().map { it.retryCount })
        }

    @Test
    fun `a failed flush emits one queue error for the user`() = runTest {
        db.mercadoDao().insert(mercadoEntity(id = "mercado-1").copy(photoUrl = "content://foto"))
        enqueue("mercado-1", createdAt = 1_000L)
        val storage = mockk<StorageService> {
            coEvery { uploadPhoto(any(), any(), any()) } throws IllegalStateException("upload rechazado")
        }
        val processor = processor(this, storage)

        errorHandler.errors.test {
            processor.flush()

            assertTrue(awaitItem() is AppError.Queue)
        }
    }

    @Test
    fun `a failed flush does not stamp lastSuccessfulFlushAt`() = runTest {
        db.mercadoDao().insert(mercadoEntity(id = "mercado-1").copy(photoUrl = "content://foto"))
        enqueue("mercado-1", createdAt = 1_000L)
        val storage = mockk<StorageService> {
            coEvery { uploadPhoto(any(), any(), any()) } throws IllegalStateException("upload rechazado")
        }
        val processor = processor(this, storage)

        processor.flush()

        assertNull(processor.lastSuccessfulFlushAt.value)
    }

    @Test
    fun `one failing entity does not strand a healthy one — the good rows still leave the queue`() =
        runTest {
            db.mercadoDao().insert(mercadoEntity(id = "malo").copy(photoUrl = "content://foto"))
            enqueue("malo", createdAt = 1_000L)
            enqueue("bueno", createdAt = 2_000L)
            val storage = mockk<StorageService> {
                coEvery { uploadPhoto(any(), any(), any()) } throws IllegalStateException("upload rechazado")
            }

            val outcome = processor(this, storage).flush()

            assertEquals(FlushOutcome.FAILED, outcome)
            assertEquals(listOf("malo"), db.syncOperationDao().getPending().map { it.entityId })
        }

    @Test
    fun `concurrent flushes are serialised, so no operation is pushed twice`() = runTest {
        enqueue("mercado-1", createdAt = 1_000L)
        val processor = processor(this)

        val first = async { processor.flush() }
        val second = async { processor.flush() }

        assertEquals(FlushOutcome.COMPLETED, first.await())
        // The second caller waits on the mutex and then finds an empty queue.
        assertEquals(FlushOutcome.COMPLETED, second.await())
        assertEquals(0, db.syncOperationDao().pendingCount())
    }
}
