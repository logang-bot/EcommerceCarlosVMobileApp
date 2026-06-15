package com.restrusher.ecomercecarlosv.domain.session

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val currentUser: StateFlow<AppUser?>
    /** True once the startup session-restore check has completed. */
    val isLoaded: StateFlow<Boolean>
    fun setCurrentUser(user: AppUser)
    fun clearSession()
    suspend fun signOut()
}
