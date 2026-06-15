package com.restrusher.ecomercecarlosv.presentation.navigation

import androidx.lifecycle.ViewModel
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    sessionManager: SessionManager,
    val errorHandler: GlobalErrorHandler,
) : ViewModel() {
    val currentUser = sessionManager.currentUser
    val isLoaded    = sessionManager.isLoaded
}
