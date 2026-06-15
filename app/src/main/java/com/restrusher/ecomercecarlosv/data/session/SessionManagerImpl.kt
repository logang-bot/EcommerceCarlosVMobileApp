package com.restrusher.ecomercecarlosv.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.di.ApplicationScope
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
) : SessionManager {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("current_user_id")
    }

    // Tracks whether the startup session clear has already been handled.
    // wipeLocalDataIfNeeded() should only run on explicit logout, not on the startup clear.
    private var startupDone = false

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        scope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val userId = status.session.user?.id ?: run {
                            _isLoaded.value = true
                            return@collect
                        }
                        val user = userRepository.getById(userId)
                            ?: userRepository.syncFromRemote(userId)
                        _currentUser.value = user
                        if (user != null) persistUserId(userId) else removeUserId()
                        _isLoaded.value = true
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _currentUser.value = null
                        removeUserId()
                        if (startupDone) wipeLocalDataIfNeeded()
                        startupDone = true
                        _isLoaded.value = true
                    }
                    is SessionStatus.Initializing -> {
                        _isLoaded.value = false
                    }
                    is SessionStatus.RefreshFailure -> {
                        // Offline: serve from local Room cache if we know the user
                        val cachedId = dataStore.data.first()[USER_ID_KEY]
                        if (cachedId != null) {
                            _currentUser.value = userRepository.getById(cachedId)
                        }
                        _isLoaded.value = true
                    }
                }
            }
        }
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
        try {
            supabase.auth.signOut()
        } catch (_: Exception) {
            // Clear local state even if the network call fails
        }
        _currentUser.value = null
        removeUserId()
    }

    private suspend fun wipeLocalDataIfNeeded() {
        if (!userRepository.hasBiometricEnabled()) {
            withContext(Dispatchers.IO) { database.clearAllTables() }
        }
    }

    private suspend fun persistUserId(userId: String) {
        dataStore.edit { prefs -> prefs[USER_ID_KEY] = userId }
    }

    private suspend fun removeUserId() {
        dataStore.edit { prefs -> prefs.remove(USER_ID_KEY) }
    }
}
