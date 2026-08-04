package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionManager : SessionManager {

    /** What [ensureValidSession] answers. */
    var sessionResult = SessionResult.VALID

    /** Every `verifiedUserId` passed to [ensureValidSession], in order. */
    val ensureValidSessionCalls = mutableListOf<String?>()

    var forgetDeviceCount = 0
        private set
    var deviceOwner: String? = null

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _currentUser
    override val isLoaded: StateFlow<Boolean> = MutableStateFlow(true)
    override val sessionRecovered: SharedFlow<Unit> = MutableSharedFlow()
    override val sessionEnded: SharedFlow<Unit> = MutableSharedFlow()

    override fun setCurrentUser(user: AppUser) {
        _currentUser.value = user
    }

    override fun clearSession() {
        _currentUser.value = null
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }

    override suspend fun ensureValidSession(verifiedUserId: String?): SessionResult {
        ensureValidSessionCalls += verifiedUserId
        return sessionResult
    }

    override suspend fun forgetDevice() {
        forgetDeviceCount++
    }

    override suspend fun canRestoreSession(userId: String): Boolean = true

    override suspend fun deviceOwnerUserId(): String? = deviceOwner

    override suspend fun claimDevice(userId: String) {
        deviceOwner = userId
    }

    override suspend fun releaseDevice() {
        deviceOwner = null
    }
}
