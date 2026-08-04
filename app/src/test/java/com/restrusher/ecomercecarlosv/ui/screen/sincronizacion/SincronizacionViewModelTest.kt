package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.data.queue.QueueProcessor
import com.restrusher.ecomercecarlosv.fakes.FakeSyncOperationDao
import com.restrusher.ecomercecarlosv.fixtures.syncOperation
import com.restrusher.ecomercecarlosv.support.MainDispatcherRule
import com.restrusher.ecomercecarlosv.ui.common.SyncIconState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * No Robolectric here — this ViewModel takes no route, so it constructs on a plain JVM test.
 * `QueueProcessor` is mocked because it is a concrete class wired to Supabase and WorkManager.
 */
class SincronizacionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeSyncOperationDao()
    private val lastFlush = MutableStateFlow<Long?>(null)
    private val queueProcessor = mockk<QueueProcessor>(relaxed = true) {
        every { lastSuccessfulFlushAt } returns lastFlush
    }

    private fun viewModel() = SincronizacionViewModel(dao, queueProcessor)

    @Test
    fun `empty queue — reports SYNCED with no items`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem()

            assertEquals(SyncIconState.SYNCED, state.syncState)
            assertEquals(emptyList<SyncQueueItem>(), state.items)
        }
    }

    @Test
    fun `queued operations that have never failed — reports PENDING`() = runTest {
        dao.given(syncOperation(id = 1L), syncOperation(id = 2L))

        viewModel().uiState.test {
            val state = awaitItem()

            assertEquals(SyncIconState.PENDING, state.syncState)
            assertEquals(2, state.items.size)
        }
    }

    @Test
    fun `a single retried operation — reports ERROR even alongside healthy ones`() = runTest {
        dao.given(syncOperation(id = 1L), syncOperation(id = 2L, retryCount = 3))

        viewModel().uiState.test {
            assertEquals(SyncIconState.ERROR, awaitItem().syncState)
        }
    }

    @Test
    fun `items — carry the queue row through to the ui`() = runTest {
        dao.given(
            syncOperation(id = 7L, entityType = "CLIENTE", operation = "DELETE", entityLabel = "Doña Ana"),
        )

        viewModel().uiState.test {
            val item = awaitItem().items.single()

            assertEquals(7L, item.id)
            assertEquals("CLIENTE", item.entityType)
            assertEquals("DELETE", item.operation)
            assertEquals("Doña Ana", item.entityLabel)
        }
    }

    @Test
    fun `lastSyncedAt — follows the queue processor`() = runTest {
        lastFlush.value = 1_700_000_000_000L

        viewModel().uiState.test {
            assertEquals(1_700_000_000_000L, awaitItem().lastSyncedAt)
        }
    }

    @Test
    fun `draining the queue — flips ERROR back to SYNCED`() = runTest {
        dao.given(syncOperation(id = 1L, retryCount = 2))

        viewModel().uiState.test {
            assertEquals(SyncIconState.ERROR, awaitItem().syncState)

            dao.delete(1L)

            assertEquals(SyncIconState.SYNCED, awaitItem().syncState)
        }
    }

    @Test
    fun `onRetry — asks the queue processor to flush`() = runTest {
        viewModel().onRetry()

        verify(exactly = 1) { queueProcessor.triggerFlush() }
    }
}
