package com.restrusher.ecomercecarlosv.presentation.navigation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    sessionManager: SessionManager,
    errorHandler: GlobalErrorHandler,
) : ViewModel() {
    val currentUser   = sessionManager.currentUser
    val isLoaded      = sessionManager.isLoaded
    val sessionEnded  = sessionManager.sessionEnded

    /**
     * Friendly message ids for the global snackbar — never the underlying technical text.
     * Repeats of the same message within [DUPLICATE_WINDOW_MS] are dropped, so a burst of related
     * failures surfaces as one message instead of a queue the user has to sit through.
     */
    val userErrors: Flow<Int> = errorHandler.errors
        .map { it.userMessageRes }
        .filter(::isNotRecentRepeat)

    private val lastShownAt = mutableMapOf<Int, Long>()

    private fun isNotRecentRepeat(@StringRes messageRes: Int): Boolean {
        val now = System.currentTimeMillis()
        if (now - (lastShownAt[messageRes] ?: 0L) < DUPLICATE_WINDOW_MS) return false
        lastShownAt[messageRes] = now
        return true
    }

    companion object {
        private const val DUPLICATE_WINDOW_MS = 5_000L
    }
}
