package com.restrusher.ecomercecarlosv.domain.session

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val currentUser: StateFlow<AppUser?>
    fun setCurrentUser(user: AppUser)
    fun clearSession()
}
