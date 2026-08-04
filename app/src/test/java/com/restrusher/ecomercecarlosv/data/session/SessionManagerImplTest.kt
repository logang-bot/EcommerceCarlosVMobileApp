package com.restrusher.ecomercecarlosv.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import com.restrusher.ecomercecarlosv.fakes.FakeNetworkMonitor
import com.restrusher.ecomercecarlosv.fakes.FakeUserRepository
import com.restrusher.ecomercecarlosv.support.createTestDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers device ownership (pure DataStore) and the three `ensureValidSession` branches that resolve
 * without a network call.
 *
 * `supabase.auth` is an **extension property**, so it is stubbed via `mockkStatic` on the accessor
 * class named in the stack trace. That is the seam the whole class hangs off: without it
 * `SessionManagerImpl` cannot even be constructed, because its `init` collects `auth.sessionStatus`.
 * The `NotAuthenticated` branch is deliberately not covered — it performs a real token refresh.
 */
@RunWith(RobolectricTestRunner::class)
class SessionManagerImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var db: AppDatabase
    private val goTrue = mockk<DataStoreGoTrueSessionManager>(relaxed = true)
    private val supabase = mockk<SupabaseClient>(relaxed = true)
    private val sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)

    @Before
    fun setUp() {
        db = createTestDatabase()
        mockkStatic("io.github.jan.supabase.auth.AuthKt")
        val auth = mockk<Auth>(relaxed = true)
        every { auth.sessionStatus } returns sessionStatus
        every { supabase.auth } returns auth
    }

    @After
    fun tearDown() {
        db.close()
        unmockkStatic("io.github.jan.supabase.auth.AuthKt")
    }

    private fun TestScope.sessionManager(): SessionManagerImpl {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(backgroundScope.coroutineContext),
        ) { tempFolder.newFile("session-${System.nanoTime()}.preferences_pb") }
        return SessionManagerImpl(
            scope = backgroundScope,
            dataStore = dataStore,
            supabase = supabase,
            userRepository = FakeUserRepository(),
            database = db,
            goTrueSessionManager = goTrue,
            networkMonitor = FakeNetworkMonitor(),
            syncOperationDao = db.syncOperationDao(),
            errorHandler = GlobalErrorHandler(),
        )
    }

    // --- device ownership -----------------------------------------------------------------

    @Test
    fun `a fresh device has no owner`() = runTest {
        assertNull(sessionManager().deviceOwnerUserId())
    }

    @Test
    fun `claimDevice records who the cached data belongs to`() = runTest {
        val manager = sessionManager()

        manager.claimDevice("user-1")

        assertEquals("user-1", manager.deviceOwnerUserId())
    }

    @Test
    fun `claiming again hands the device to the new owner`() = runTest {
        val manager = sessionManager()
        manager.claimDevice("user-1")

        manager.claimDevice("user-2")

        assertEquals("user-2", manager.deviceOwnerUserId())
    }

    @Test
    fun `releaseDevice forgets the owner, as it must when the cache is wiped`() = runTest {
        val manager = sessionManager()
        manager.claimDevice("user-1")

        manager.releaseDevice()

        assertNull(manager.deviceOwnerUserId())
    }

    @Test
    fun `releaseDevice on an unclaimed device is harmless`() = runTest {
        val manager = sessionManager()

        manager.releaseDevice()

        assertNull(manager.deviceOwnerUserId())
    }

    @Test
    fun `clearSession leaves the device owner intact, which is what detects a handover`() = runTest {
        val manager = sessionManager()
        manager.claimDevice("user-1")

        manager.clearSession()

        assertEquals("user-1", manager.deviceOwnerUserId())
        assertNull(manager.currentUser.value)
    }

    // --- session restore ------------------------------------------------------------------

    @Test
    fun `canRestoreSession — a stored refresh token means a fingerprint tap can mint a session`() =
        runTest {
            coEvery { goTrue.getLastRefreshToken("user-1") } returns "refresh-token"

            assertTrue(sessionManager().canRestoreSession("user-1"))
        }

    @Test
    fun `canRestoreSession — no stored token means the token was rejected, so demand a password`() =
        runTest {
            coEvery { goTrue.getLastRefreshToken("user-1") } returns null

            assertFalse(sessionManager().canRestoreSession("user-1"))
        }

    // --- ensureValidSession ---------------------------------------------------------------

    @Test
    fun `an authenticated session is reported VALID`() = runTest {
        val manager = sessionManager()
        sessionStatus.value = mockk<SessionStatus.Authenticated>(relaxed = true)

        assertEquals(SessionResult.VALID, manager.ensureValidSession())
    }

    @Test
    fun `while supabase is still initializing the answer is DEFERRED, never VALID`() = runTest {
        val manager = sessionManager()
        sessionStatus.value = SessionStatus.Initializing

        assertEquals(SessionResult.DEFERRED, manager.ensureValidSession())
    }

    @Test
    fun `a refresh failure defers instead of refreshing, so the token family is not revoked`() =
        runTest {
            val manager = sessionManager()
            sessionStatus.value = mockk<SessionStatus.RefreshFailure>(relaxed = true)

            assertEquals(SessionResult.DEFERRED, manager.ensureValidSession())
        }
}
