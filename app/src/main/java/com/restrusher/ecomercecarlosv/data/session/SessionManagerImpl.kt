package com.restrusher.ecomercecarlosv.data.session

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.di.ApplicationScope
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : SessionManager {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("current_user_id")
        private const val TAG = "SessionManagerImpl"
    }

    // Serialises refreshes so concurrent callers share one in-flight attempt.
    private val refreshMutex = Mutex()

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        scope.launch {
            supabase.auth.sessionStatus.collect(::handleSessionStatus)
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
    // with it — nothing on this device should outlive a sign-out that has no way back in.
    private suspend fun revokeAndWipe() {
        runCatching { supabase.auth.signOut() }
            .onFailure { Log.w(TAG, "signOut: server logout failed, clearing locally", it) }
        goTrueSessionManager.clearLastRefreshToken()
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }

    override suspend fun forgetDevice() {
        runCatching { supabase.auth.signOut(SignOutScope.GLOBAL) }
            .onFailure { Log.w(TAG, "forgetDevice: server logout failed, clearing locally", it) }
        goTrueSessionManager.clearLastRefreshToken()
        _currentUser.value = null
        removeUserId()
    }

    override suspend fun ensureValidSession(): SessionResult = refreshMutex.withLock {
        if (supabase.auth.currentSessionOrNull() != null) return@withLock SessionResult.VALID
        if (!networkMonitor.isOnline) return@withLock SessionResult.OFFLINE
        val userId = currentUserId() ?: return@withLock SessionResult.REVOKED
        val refreshToken = goTrueSessionManager.getLastRefreshToken(userId)
            ?: return@withLock SessionResult.REVOKED
        refreshWith(userId, refreshToken)
    }

    private suspend fun refreshWith(userId: String, refreshToken: String): SessionResult = try {
        val newSession = supabase.auth.refreshSession(refreshToken)
        // Persist before importing — see saveLastRefreshToken for why the order matters.
        goTrueSessionManager.saveLastRefreshToken(userId, newSession.refreshToken)
        supabase.auth.importSession(newSession)
        SessionResult.VALID
    } catch (e: RestException) {
        Log.w(TAG, "ensureValidSession: refresh token rejected — password login required", e)
        goTrueSessionManager.clearLastRefreshToken()
        SessionResult.REVOKED
    } catch (e: Exception) {
        Log.d(TAG, "ensureValidSession: could not reach Supabase — staying local-only", e)
        SessionResult.OFFLINE
    }

    private suspend fun currentUserId(): String? =
        _currentUser.value?.id ?: dataStore.data.first()[USER_ID_KEY]

    private suspend fun persistUserId(userId: String) {
        dataStore.edit { prefs -> prefs[USER_ID_KEY] = userId }
    }

    private suspend fun removeUserId() {
        dataStore.edit { prefs -> prefs.remove(USER_ID_KEY) }
    }
}
