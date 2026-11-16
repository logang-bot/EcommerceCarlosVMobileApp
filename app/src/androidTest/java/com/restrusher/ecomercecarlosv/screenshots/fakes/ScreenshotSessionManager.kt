package com.restrusher.ecomercecarlosv.screenshots.fakes

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stands in for `SessionManagerImpl`, which wipes the stored Supabase session on every launch and
 * would always land the app on Login.
 *
 * The startup check is reported as already finished, so `AppNavigation` goes straight to Home when
 * a test has called [setCurrentUser] before launching the activity. [ensureValidSession] answers
 * [SessionResult.OFFLINE], which makes `DataSynchronizer` skip every remote read silently, with no
 * request and no error snackbar.
 */
@Singleton
class ScreenshotSessionManager @Inject constructor() : SessionManager {

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

    override suspend fun ensureValidSession(verifiedUserId: String?): SessionResult = SessionResult.OFFLINE

    override suspend fun forgetDevice() = Unit

    override suspend fun canRestoreSession(userId: String): Boolean = false

    override suspend fun deviceOwnerUserId(): String? = _currentUser.value?.id

    override suspend fun claimDevice(userId: String) = Unit

    override suspend fun releaseDevice() = Unit
}
