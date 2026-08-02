package com.restrusher.ecomercecarlosv.domain.session

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val currentUser: StateFlow<AppUser?>
    /** True once the startup session-restore check has completed. */
    val isLoaded: StateFlow<Boolean>

    /**
     * Emits whenever a usable access token comes back after the app was without one. Subscribers
     * should use it to retry the work they had to skip: the write queue flushes, the synchronizer
     * marks its data stale. Without this the only thing eventually retrying is WorkManager's
     * exponential backoff, which turns a ~10s recovery into ~30s.
     */
    val sessionRecovered: SharedFlow<Unit>

    /**
     * Emits when the stored refresh token is rejected and there is nothing left to authenticate the
     * user with — only a password login can recover. The local session has already been cleared by
     * the time this fires; the navigation layer is expected to send the user back to Login, since
     * telling them their session expired without moving them there asks for something the app gives
     * them no way to do.
     */
    val sessionEnded: SharedFlow<Unit>
    fun setCurrentUser(user: AppUser)
    fun clearSession()

    /**
     * Ends the session. On a device with fingerprint enrolled the refresh token is deliberately
     * kept and only the access token is dropped, so the next fingerprint tap can mint a fresh
     * session without a password. Otherwise the refresh token is revoked server-side as well.
     */
    suspend fun signOut()

    /**
     * Guarantees a usable Supabase access token before RLS-protected work runs.
     *
     * Refreshes from the stored token **only** when supabase-kt has torn its own retry loop down.
     * While that loop is alive the answer is [SessionResult.DEFERRED] — wait on [sessionRecovered]
     * rather than refreshing, because two clients spending the same rotating token looks like a
     * stolen-token replay and costs the user the whole token family.
     *
     * Safe to call concurrently; callers share a single in-flight attempt.
     *
     * @param verifiedUserId names the account to restore, and asserts the caller has just verified
     *   that person locally — a successful fingerprint prompt. Only the login flow may pass it. Every
     *   other caller omits it and is resolved from the active session, so a background queue flush
     *   can never authenticate someone who has not signed in yet: at the login screen the app has
     *   deliberately forgotten who the user is.
     */
    suspend fun ensureValidSession(verifiedUserId: String? = null): SessionResult

    /**
     * Hard sign-out for "forget this device": revokes the refresh token across all sessions and
     * erases the local copy. Unlike [signOut] this never wipes cached data — the caller decides
     * what happens to it.
     */
    suspend fun forgetDevice()

    /**
     * True while [userId] still has a stored refresh token, i.e. a fingerprint tap can mint a
     * session without a password. False means Supabase rejected it and only a password login
     * recovers — the login screen uses this to arm the password field instead of offering a
     * fingerprint that cannot work. Answered locally, so it is also correct offline.
     */
    suspend fun canRestoreSession(userId: String): Boolean

    /**
     * The user whose data is cached on this device, or null if nothing is cached.
     *
     * Unlike [currentUser] this survives sign-out, the startup session wipe and session expiry, so
     * it is the only thing that can tell a returning user from a different one at the login screen.
     * Deliberately **not** written when a session merely becomes authenticated: it is claimed via
     * [claimDevice] once a login has been allowed to keep the cache, which is what lets a handover
     * be detected in the window between signing in and resolving it.
     */
    suspend fun deviceOwnerUserId(): String?

    /** Records [userId] as the owner of the data cached on this device. */
    suspend fun claimDevice(userId: String)

    /** Forgets the device owner. Call whenever the cached data it referred to is wiped. */
    suspend fun releaseDevice()
}
