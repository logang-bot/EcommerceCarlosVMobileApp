package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import javax.inject.Inject

/** Outcome of a fingerprint login attempt. */
sealed interface BiometricLoginResult {
    /**
     * @param isFreshSession true when a new access token was minted, meaning remote reads and
     *   writes will work. False for an offline login served from the local cache.
     */
    data class Success(val user: AppUser, val isFreshSession: Boolean) : BiometricLoginResult
    data object AccountDisabled : BiometricLoginResult
    data object PasswordRequired : BiometricLoginResult
    data object NotEnrolled : BiometricLoginResult
}

/**
 * Turns a successful fingerprint prompt into a real, server-backed session.
 *
 * The fingerprint only unlocks the device — it proves nothing to Supabase. So when online this
 * trades the stored refresh token for a brand-new access token and re-reads the profile, applying
 * the same role and is-active checks as a password login. It deliberately blocks until that
 * finishes: navigating first is what previously dropped users into the app with no session, where
 * every request went out anonymous and was silently rejected by RLS.
 */
class BiometricLoginUseCase @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): BiometricLoginResult {
        val enrolled = userRepository.getBiometricEnabledUser() ?: return BiometricLoginResult.NotEnrolled
        return when (sessionManager.ensureValidSession()) {
            SessionResult.VALID -> loginWithFreshSession(enrolled)
            SessionResult.OFFLINE -> loginFromCache(enrolled)
            SessionResult.REVOKED -> BiometricLoginResult.PasswordRequired
        }
    }

    private suspend fun loginWithFreshSession(enrolled: AppUser): BiometricLoginResult {
        val user = userRepository.syncFromRemote(enrolled.id) ?: enrolled
        if (!user.isActive) return revokeDisabledAccount(user)
        return BiometricLoginResult.Success(user, isFreshSession = true)
    }

    private fun loginFromCache(enrolled: AppUser): BiometricLoginResult =
        if (enrolled.isActive) {
            BiometricLoginResult.Success(enrolled, isFreshSession = false)
        } else {
            BiometricLoginResult.AccountDisabled
        }

    // A deactivated account loses its device enrolment, so the fingerprint stops offering a way in
    // until a superuser reactivates it and the user signs in with their password again.
    private suspend fun revokeDisabledAccount(user: AppUser): BiometricLoginResult {
        sessionManager.forgetDevice()
        userRepository.setBiometricEnabled(user.id, null)
        return BiometricLoginResult.AccountDisabled
    }
}
