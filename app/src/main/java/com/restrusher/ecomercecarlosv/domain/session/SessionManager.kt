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

    /**
     * Best-effort, fire-and-forget: silently re-establishes a real Supabase session for the
     * biometric-enrolled user using its last stored refresh token, so RLS-protected sync/writes
     * work once the device is online. Runs on an application-scoped coroutine — safe to call
     * right before navigating away (e.g. right after a fingerprint-only login), since it must
     * outlive the caller's own scope. Silently no-ops when offline, when no token is stored, or
     * when the token is rejected; callers should never treat that as a login failure.
     */
    fun restoreBiometricSession()

    /** Clears the stored refresh token used by [restoreBiometricSession]. */
    suspend fun clearBiometricSession()
}
