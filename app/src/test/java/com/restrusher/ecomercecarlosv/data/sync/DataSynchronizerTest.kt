package com.restrusher.ecomercecarlosv.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.sync.impl.ClienteSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.MercadoSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.PedidoSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.ProductoSyncer
import com.restrusher.ecomercecarlosv.data.sync.impl.UmbralesSyncer
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import com.restrusher.ecomercecarlosv.fakes.FakeNetworkMonitor
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataSynchronizerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val network = FakeNetworkMonitor()
    private val session = FakeSessionManager()
    private val errorHandler = GlobalErrorHandler()
    private val mercadoSyncer = mockk<MercadoSyncer>()

    @Before
    fun clearPersistedStaleness() {
        context.getSharedPreferences("sync_staleness", Context.MODE_PRIVATE).edit().clear().commit()
        coEvery { mercadoSyncer.sync(any()) } returns SyncResult.Success
    }

    /**
     * The app scope must not be the test's own: `isSyncing` is a `stateIn(Eagerly)` that never
     * completes, so `runTest` would wait on it forever. `backgroundScope` is cancelled at test end,
     * and the unconfined dispatcher makes `triggerSyncIfStale`'s fire-and-forget launch run eagerly
     * instead of waiting for the scheduler.
     */
    private fun TestScope.synchronizer() = DataSynchronizer(
        context = context,
        networkMonitor = network,
        mercadoSyncer = mercadoSyncer,
        clienteSyncer = mockk<ClienteSyncer>(relaxed = true),
        productoSyncer = mockk<ProductoSyncer>(relaxed = true),
        pedidoSyncer = mockk<PedidoSyncer>(relaxed = true),
        umbralesSyncer = mockk<UmbralesSyncer>(relaxed = true),
        errorHandler = errorHandler,
        sessionManager = session,
        appScope = CoroutineScope(
            backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
        ),
    )

    @Test
    fun `offline — fails immediately without asking for a session`() = runTest {
        network.setOnline(false)

        assertFalse(synchronizer().forceSync(EntityType.MERCADO))
        assertTrue(session.ensureValidSessionCalls.isEmpty())
        coVerify(exactly = 0) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `no session — fails without running the syncer`() = runTest {
        session.sessionResult = SessionResult.OFFLINE

        assertFalse(synchronizer().forceSync(EntityType.MERCADO))
        coVerify(exactly = 0) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `a revoked session fails the read rather than retrying`() = runTest {
        session.sessionResult = SessionResult.REVOKED

        assertFalse(synchronizer().forceSync(EntityType.MERCADO))
        coVerify(exactly = 0) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `first sync — runs a full fetch, since there is no cursor yet`() = runTest {
        val since = slot<Long>()
        coEvery { mercadoSyncer.sync(capture(since)) } returns SyncResult.Success

        assertTrue(synchronizer().forceSync(EntityType.MERCADO))
        assertEquals(0L, since.captured)
    }

    @Test
    fun `second sync — passes the previous timestamp as a delta cursor`() = runTest {
        val sincesSeen = mutableListOf<Long>()
        coEvery { mercadoSyncer.sync(capture(sincesSeen)) } returns SyncResult.Success
        val synchronizer = synchronizer()

        synchronizer.forceSync(EntityType.MERCADO)
        synchronizer.forceSync(EntityType.MERCADO)

        assertEquals(0L, sincesSeen.first())
        assertTrue("expected a delta cursor, got ${sincesSeen.last()}", sincesSeen.last() > 0L)
    }

    @Test
    fun `a failed sync is retried once before being reported`() = runTest {
        coEvery { mercadoSyncer.sync(any()) } returns SyncResult.Failure(IllegalStateException("boom"))

        assertFalse(synchronizer().forceSync(EntityType.MERCADO))
        coVerify(exactly = 2) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `a sync that fails twice rolls the cursor back, so the next attempt refetches everything`() =
        runTest {
            val sincesSeen = mutableListOf<Long>()
            coEvery { mercadoSyncer.sync(capture(sincesSeen)) } returns
                SyncResult.Failure(IllegalStateException("boom"))
            val synchronizer = synchronizer()

            synchronizer.forceSync(EntityType.MERCADO)
            synchronizer.forceSync(EntityType.MERCADO)

            assertTrue("cursor should never advance on failure", sincesSeen.all { it == 0L })
        }

    @Test
    fun `a transient failure followed by a success is not reported to the user`() = runTest {
        coEvery { mercadoSyncer.sync(any()) } returnsMany
            listOf(SyncResult.Failure(IllegalStateException("blip")), SyncResult.Success)

        assertTrue(synchronizer().forceSync(EntityType.MERCADO))
    }

    @Test
    fun `a sync that never returns times out, twice, and fails`() = runTest {
        coEvery { mercadoSyncer.sync(any()) } coAnswers {
            delay(Long.MAX_VALUE)
            SyncResult.Success
        }

        assertFalse(synchronizer().forceSync(EntityType.MERCADO))
        coVerify(exactly = 2) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `an unknown entity type fails without touching any syncer`() = runTest {
        assertFalse(synchronizer().forceSync("MARCIANO"))
        coVerify(exactly = 0) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `fresh data short-circuits before the session is checked at all`() = runTest {
        val synchronizer = synchronizer()
        synchronizer.forceSync(EntityType.MERCADO)
        val callsAfterFirstSync = session.ensureValidSessionCalls.size
        session.sessionResult = SessionResult.OFFLINE

        synchronizer.triggerSyncIfStale(EntityType.MERCADO, DataSynchronizer.THRESHOLD_MASTER_MS)
        advanceUntilIdle()

        // The whole point of the ordering: a screen with current data never waits on a session.
        assertEquals(callsAfterFirstSync, session.ensureValidSessionCalls.size)
        coVerify(exactly = 1) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `stale data does trigger a sync`() = runTest {
        val synchronizer = synchronizer()
        synchronizer.forceSync(EntityType.MERCADO)

        synchronizer.triggerSyncIfStale(EntityType.MERCADO, thresholdMs = 0L)
        advanceUntilIdle()

        coVerify(exactly = 2) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `resetStaleness makes everything stale again`() = runTest {
        val synchronizer = synchronizer()
        synchronizer.forceSync(EntityType.MERCADO)

        synchronizer.resetStaleness()
        synchronizer.triggerSyncIfStale(EntityType.MERCADO, DataSynchronizer.THRESHOLD_MASTER_MS)
        advanceUntilIdle()

        coVerify(exactly = 2) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `forceSync ignores the staleness threshold entirely`() = runTest {
        val synchronizer = synchronizer()
        synchronizer.forceSync(EntityType.MERCADO)

        synchronizer.forceSync(EntityType.MERCADO)

        coVerify(exactly = 2) { mercadoSyncer.sync(any()) }
    }

    @Test
    fun `the master and business thresholds are two hours and thirty minutes`() {
        assertEquals(2 * 60 * 60 * 1000L, DataSynchronizer.THRESHOLD_MASTER_MS)
        assertEquals(30 * 60 * 1000L, DataSynchronizer.THRESHOLD_BUSINESS_MS)
    }
}
