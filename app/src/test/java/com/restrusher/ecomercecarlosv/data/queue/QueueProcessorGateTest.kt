package com.restrusher.ecomercecarlosv.data.queue

import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fixtures.appUser
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import com.restrusher.ecomercecarlosv.support.queueProcessorOver
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The two gates in front of the push loop. Both must return DEFERRED **without** burning a retry:
 * nothing was attempted, and the operations themselves are fine.
 */
@RunWith(RobolectricTestRunner::class)
class QueueProcessorGateTest {

    private lateinit var db: AppDatabase
    private val session = FakeSessionManager()

    @Before
    fun setUp() {
        db = createTestDatabase()
    }

    @After
    fun tearDown() = db.close()

    private fun processor(scope: TestScope) =
        queueProcessorOver(db, scope, sessionManager = session)

    private suspend fun enqueueOne() = db.syncOperationDao().enqueue(
        SyncOperationEntity(
            entityType = EntityType.MERCADO,
            entityId = "mercado-1",
            operation = SyncOp.UPSERT,
        ),
    )

    private suspend fun retryCounts() = db.syncOperationDao().getPending().map { it.retryCount }

    @Test
    fun `an empty queue completes without even asking for a session`() = runTest {
        val outcome = processor(this).flush()

        assertEquals(FlushOutcome.COMPLETED, outcome)
        assertEquals(emptyList<String?>(), session.ensureValidSessionCalls)
    }

    @Test
    fun `offline — defers and burns no retry`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.OFFLINE

        val outcome = processor(this).flush()

        assertEquals(FlushOutcome.DEFERRED, outcome)
        assertEquals(listOf(0), retryCounts())
    }

    @Test
    fun `session still renewing — defers and burns no retry`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.DEFERRED

        val outcome = processor(this).flush()

        assertEquals(FlushOutcome.DEFERRED, outcome)
        assertEquals(listOf(0), retryCounts())
    }

    @Test
    fun `revoked session — defers rather than failing, since nothing was attempted`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.REVOKED

        val outcome = processor(this).flush()

        assertEquals(FlushOutcome.DEFERRED, outcome)
        assertEquals(listOf(0), retryCounts())
    }

    @Test
    fun `a deferred flush leaves the queue untouched`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.OFFLINE

        processor(this).flush()

        assertEquals(1, db.syncOperationDao().pendingCount())
    }

    @Test
    fun `the flush asks for a session without naming a user, so it cannot authenticate anyone`() =
        runTest {
            enqueueOne()
            session.sessionResult = SessionResult.OFFLINE

            processor(this).flush()

            assertEquals(listOf<String?>(null), session.ensureValidSessionCalls)
        }

    @Test
    fun `queue owned by another user — defers so their writes are not filed under this account`() =
        runTest {
            enqueueOne()
            session.sessionResult = SessionResult.VALID
            session.deviceOwner = "user-anterior"
            session.setCurrentUser(appUser(id = "user-nuevo"))

            val outcome = processor(this).flush()

            assertEquals(FlushOutcome.DEFERRED, outcome)
            assertEquals(listOf(0), retryCounts())
            assertEquals(1, db.syncOperationDao().pendingCount())
        }

    @Test
    fun `queue owned by the signed-in user — proceeds past the ownership gate`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.VALID
        session.deviceOwner = "user-1"
        session.setCurrentUser(appUser(id = "user-1"))

        val outcome = processor(this).flush()

        // The mercado row does not exist locally, so the upsert is a no-op and the flush completes.
        assertEquals(FlushOutcome.COMPLETED, outcome)
    }

    @Test
    fun `no device owner recorded yet — proceeds rather than blocking the first login`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.VALID
        session.deviceOwner = null

        assertEquals(FlushOutcome.COMPLETED, processor(this).flush())
    }

    @Test
    fun `nobody signed in — proceeds, since there is no conflicting account`() = runTest {
        enqueueOne()
        session.sessionResult = SessionResult.VALID
        session.deviceOwner = "user-anterior"

        assertEquals(FlushOutcome.COMPLETED, processor(this).flush())
    }
}
