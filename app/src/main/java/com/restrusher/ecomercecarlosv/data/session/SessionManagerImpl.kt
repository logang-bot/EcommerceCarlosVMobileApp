package com.restrusher.ecomercecarlosv.data.session

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.di.ApplicationScope
import com.restrusher.ecomercecarlosv.domain.error.AppError
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
    private val supabase: SupabaseClient,
    private val userRepository: UserRepository,
    private val database: AppDatabase,
    private val goTrueSessionManager: DataStoreGoTrueSessionManager,
    private val networkMonitor: NetworkMonitor,
    private val syncOperationDao: SyncOperationDao,
    private val errorHandler: GlobalErrorHandler,
) : SessionManager {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("current_user_id")

        // Who the data cached on this device belongs to. Outlives USER_ID_KEY on purpose: that one
        // is cleared by the startup wipe and by endSession, which is exactly when we still need to
        // know whose pedidos are sitting in the queue. See SessionManager.deviceOwnerUserId.
        private val DEVICE_OWNER_KEY = stringPreferencesKey("device_owner_user_id")

        private const val TAG = "SessionManagerImpl"

        /** Server-side failures worth retrying, as opposed to the token actually being rejected. */
        private val TRANSIENT_STATUS_RANGE = 500..599
    }

    // Serialises refreshes so concurrent callers share one in-flight attempt.
    private val refreshMutex = Mutex()

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _sessionRecovered = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val sessionRecovered: SharedFlow<Unit> = _sessionRecovered.asSharedFlow()

    private val _sessionEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val sessionEnded: SharedFlow<Unit> = _sessionEnded.asSharedFlow()

    init {
        scope.launch {
            supabase.auth.sessionStatus.collect(::handleSessionStatus)
        }
        // An offline fingerprint login leaves no session and no supabase-kt retry loop, so nothing
        // would restore it on its own — sessionRecovered never fires for a session that never
        // existed. This is the only wake-up for that case.
        scope.launch {
            networkMonitor.isOnlineFlow
                .drop(1)
                .distinctUntilChanged()
                .collect { isOnline -> if (isOnline) ensureValidSession() }
        }
    }

    private suspend fun handleSessionStatus(status: SessionStatus) {
        when (status) {
            is SessionStatus.Authenticated -> onAuthenticated(status)
            is SessionStatus.NotAuthenticated -> onNotAuthenticated()
            is SessionStatus.Initializing -> _isLoaded.value = false
            // Offline or refresh failure: serve from the local Room cache if we know the user.
            is SessionStatus.RefreshFailure -> {
                dataStore.data.first()[USER_ID_KEY]?.let { _currentUser.value = userRepository.getById(it) }
                _isLoaded.value = true
            }
        }
    }

    private suspend fun onAuthenticated(status: SessionStatus.Authenticated) {
        val userId = status.session.user?.id
        if (userId == null) {
            _isLoaded.value = true
            return
        }
        val user = userRepository.getById(userId) ?: userRepository.syncFromRemote(userId)
        _currentUser.value = user
        if (user != null) persistUserId(userId) else removeUserId()
        _isLoaded.value = true
        if (status.source is SessionSource.Refresh) announceRecovery(userId)
    }

    // A renewed token means everything skipped while we had none can go now. Also re-reads the
    // profile: the cached row can predate a role change made while this device was offline, and
    // onAuthenticated above only fetches remotely when the local row is missing entirely.
    private suspend fun announceRecovery(userId: String) {
        userRepository.syncFromRemote(userId)?.let { _currentUser.value = it }
        _sessionRecovered.tryEmit(Unit)
    }

    // Deliberately never wipes local data. NotAuthenticated also fires for the startup session
    // clear and for a mid-session refresh failure, and wiping on those would destroy cached
    // business data — including unsynced queue operations — behind the user's back. Only an
    // explicit sign-out wipes, and it does so directly in [signOut].
    private suspend fun onNotAuthenticated() {
        _currentUser.value = null
        removeUserId()
        _isLoaded.value = true
    }

    override fun setCurrentUser(user: AppUser) {
        _currentUser.value = user
        scope.launch { persistUserId(user.id) }
    }

    override fun clearSession() {
        _currentUser.value = null
        scope.launch { removeUserId() }
    }

    override suspend fun signOut() {
        if (userRepository.hasBiometricEnabled()) discardAccessToken() else revokeAndWipe()
        _currentUser.value = null
        removeUserId()
    }

    // Enrolled device: drop the access token locally but leave the refresh token intact so the next
    // fingerprint tap can mint a fresh session without a password. An access token already copied
    // off the device stays usable until it expires — bounded by its ~1h lifetime.
    private suspend fun discardAccessToken() {
        runCatching { supabase.auth.clearSession() }
            .onFailure { Log.w(TAG, "signOut: could not clear local session", it) }
    }

    // No fingerprint to come back with, so the refresh token is revoked and the cached data goes
    // with it — nothing on this device should outlive a sign-out that has no way back in. Queued
    // writes are the exception: they exist nowhere else, so they are kept for the next login to
    // push (or to be resolved as a handover if somebody else signs in instead).
    private suspend fun revokeAndWipe() {
        runCatching { supabase.auth.signOut() }
            .onFailure { Log.w(TAG, "signOut: server logout failed, clearing locally", it) }
        goTrueSessionManager.clearLastRefreshToken()
        val pending = syncOperationDao.pendingCount()
        if (pending > 0) {
            Log.w(TAG, "signOut: keeping cached data — $pending operation(s) still unsynced")
            return
        }
        wipeCachedData()
    }

    private suspend fun wipeCachedData() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        releaseDevice()
    }

    override suspend fun forgetDevice() {
        runCatching { supabase.auth.signOut(SignOutScope.GLOBAL) }
            .onFailure { Log.w(TAG, "forgetDevice: server logout failed, clearing locally", it) }
        goTrueSessionManager.clearLastRefreshToken()
        _currentUser.value = null
        removeUserId()
        // The device owner deliberately survives: this never wipes cached data, and whatever is
        // still queued belongs to that user until someone decides its fate at the next login.
    }

    override suspend fun ensureValidSession(verifiedUserId: String?): SessionResult = refreshMutex.withLock {
        when (supabase.auth.sessionStatus.value) {
            is SessionStatus.Authenticated -> SessionResult.VALID
            // supabase-kt's retry loop is alive and still holds this refresh token. Refreshing it
            // ourselves would spend a token the loop replays ~10s later, which Supabase reads as a
            // stolen-token replay and answers by revoking the whole family. Wait it out instead.
            is SessionStatus.RefreshFailure, SessionStatus.Initializing -> SessionResult.DEFERRED
            is SessionStatus.NotAuthenticated -> refreshFromStoredToken(verifiedUserId)
        }
    }

    // Only reached once supabase-kt has torn its own loop down, so no competing refresh exists.
    private suspend fun refreshFromStoredToken(verifiedUserId: String?): SessionResult {
        if (!networkMonitor.isOnline) return SessionResult.OFFLINE
        // Nobody is signed in on this device — a fresh install, or before the first login. Not an
        // expired session, so stay silent: reporting REVOKED here would fire "tu sesión expiró" at
        // someone who never had one, e.g. when startup finds orphaned queue rows.
        val userId = verifiedUserId ?: currentUserId() ?: return SessionResult.DEFERRED
        val refreshToken = goTrueSessionManager.getLastRefreshToken(userId)
            ?: return endSession()
        return refreshWith(userId, refreshToken)
    }

    // We know who the user is but have nothing left to authenticate them with. Drop the local
    // session so the UI stops presenting them as signed in, and tell the app to send them to Login —
    // a message alone would ask for something the app gives them no way to do.
    //
    // The error is emitted here rather than by the callers so that every path is covered: the
    // reconnect collector above calls ensureValidSession with nobody to report on its behalf, which
    // used to teleport the user to Login with no explanation at all.
    private suspend fun endSession(): SessionResult {
        _currentUser.value = null
        removeUserId()
        errorHandler.emit(AppError.Session("refresh token rejected — password login required"))
        _sessionEnded.tryEmit(Unit)
        return SessionResult.REVOKED
    }

    private suspend fun refreshWith(userId: String, refreshToken: String): SessionResult = try {
        val newSession = supabase.auth.refreshSession(refreshToken)
        // Persist before importing — see saveLastRefreshToken for why the order matters.
        goTrueSessionManager.saveLastRefreshToken(userId, newSession.refreshToken)
        supabase.auth.importSession(newSession)
        // importSession reports source = Unknown, so onAuthenticated will not announce this one.
        announceRecovery(userId)
        SessionResult.VALID
    } catch (e: RestException) {
        // A 5xx is Supabase having a bad day, not the token being rejected. Signing the user out
        // over an outage would strand them behind a password prompt for something that was never
        // their problem — so keep the token and let the next attempt retry, exactly as supabase-kt
        // does in tryImportingSession.
        if (e.statusCode in TRANSIENT_STATUS_RANGE) {
            Log.w(TAG, "ensureValidSession: server error ${e.statusCode} — keeping token, will retry", e)
            SessionResult.OFFLINE
        } else {
            Log.w(TAG, "ensureValidSession: refresh token rejected — password login required", e)
            goTrueSessionManager.clearLastRefreshToken()
            endSession()
        }
    } catch (e: Exception) {
        Log.d(TAG, "ensureValidSession: could not reach Supabase — staying local-only", e)
        SessionResult.OFFLINE
    }

    // Deliberately does NOT fall back to the enrolled biometric user. At the login screen both of
    // these are null (the startup wipe emits NotAuthenticated, which clears the stored id), so such
    // a fallback would let a background queue flush authenticate before the user has proved anything
    // — exactly what the always-require-login-on-start design exists to prevent. Callers that have
    // verified the user pass the id explicitly instead.
    private suspend fun currentUserId(): String? =
        _currentUser.value?.id ?: dataStore.data.first()[USER_ID_KEY]

    // The stored token is cleared the moment Supabase rejects it (see refreshWith), and the only
    // other places that clear it also drop the fingerprint enrolment. So for an enrolled user its
    // absence means exactly one thing: the token was rejected and a password is now required.
    override suspend fun canRestoreSession(userId: String): Boolean =
        goTrueSessionManager.getLastRefreshToken(userId) != null

    override suspend fun deviceOwnerUserId(): String? = dataStore.data.first()[DEVICE_OWNER_KEY]

    override suspend fun claimDevice(userId: String) {
        dataStore.edit { prefs -> prefs[DEVICE_OWNER_KEY] = userId }
    }

    override suspend fun releaseDevice() {
        dataStore.edit { prefs -> prefs.remove(DEVICE_OWNER_KEY) }
    }

    private suspend fun persistUserId(userId: String) {
        dataStore.edit { prefs -> prefs[USER_ID_KEY] = userId }
    }

    private suspend fun removeUserId() {
        dataStore.edit { prefs -> prefs.remove(USER_ID_KEY) }
    }
}
