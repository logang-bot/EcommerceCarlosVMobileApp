package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import javax.inject.Inject

/**
 * "Olvidar este dispositivo": removes every trace of the enrolled user from this phone.
 *
 * Revokes the refresh token across all sessions so a copy lifted off the device is dead, drops the
 * fingerprint enrolment, and clears the cached business data — the latter only once the write queue
 * has drained, since unsynced pedidos exist nowhere else. The remote account itself is untouched.
 */
class ForgetEnrolledUserUseCase @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val deviceDataCleaner: DeviceDataCleaner,
) {
    suspend operator fun invoke() {
        val enrolled = userRepository.getBiometricEnabledUser()
        // Order matters: the cleaner pushes anything still queued, which needs a live session.
        // Revoking first would strand those writes on a device that is about to forget them.
        deviceDataCleaner.wipeCachedDataIfFullySynced()
        sessionManager.forgetDevice()
        enrolled?.let { userRepository.setBiometricEnabled(it.id, null) }
    }
}
