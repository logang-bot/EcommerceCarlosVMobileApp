package com.restrusher.ecomercecarlosv.data.session

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// TODO: Persist session to DataStore so it survives process death — Phase 9 (Supabase auth)
@Singleton
class SessionManagerImpl @Inject constructor() : SessionManager {

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    override fun setCurrentUser(user: AppUser) {
        _currentUser.value = user
    }

    override fun clearSession() {
        _currentUser.value = null
    }
}
