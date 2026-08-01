package com.restrusher.ecomercecarlosv.domain.session

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val currentUser: StateFlow<AppUser?>
    /** True once the startup session-restore check has completed. */
    val isLoaded: StateFlow<Boolean>
    fun setCurrentUser(user: AppUser)
    fun clearSession()

    /**
     * Ends the session. On a device with fingerprint enrolled the refresh token is deliberately
     * kept and only the access token is dropped, so the next fingerprint tap can mint a fresh
     * session without a password. Otherwise the refresh token is revoked server-side as well.
     */
    suspend fun signOut()

    /**
     * Guarantees a usable Supabase access token before RLS-protected work runs, trading the stored
     * refresh token for a brand-new session when needed. Safe to call concurrently — callers share
     * a single in-flight refresh rather than racing, since parallel refreshes with the same
     * rotating token would themselves look like a token-reuse attack.
     */
    suspend fun ensureValidSession(): SessionResult

    /**
     * Hard sign-out for "forget this device": revokes the refresh token across all sessions and
     * erases the local copy. Unlike [signOut] this never wipes cached data — the caller decides
     * what happens to it.
     */
    suspend fun forgetDevice()
}
