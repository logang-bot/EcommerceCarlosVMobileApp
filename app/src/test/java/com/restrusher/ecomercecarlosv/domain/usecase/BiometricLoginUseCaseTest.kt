package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.session.SessionResult
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fakes.FakeUserRepository
import com.restrusher.ecomercecarlosv.fixtures.appUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricLoginUseCaseTest {

    private val sessionManager = FakeSessionManager()
    private val userRepository = FakeUserRepository()
    private val useCase = BiometricLoginUseCase(sessionManager, userRepository)

    private val enrolled = appUser(id = "user-1", name = "Carlos Vargas", biometricEnabledAt = 1L)

    @Test
    fun `nobody enrolled — reports NotEnrolled without asking for a session`() = runTest {
        userRepository.biometricEnabledUser = null

        assertEquals(BiometricLoginResult.NotEnrolled, useCase())
        assertTrue(sessionManager.ensureValidSessionCalls.isEmpty())
    }

    @Test
    fun `fresh session — names the enrolled user, since the login screen has forgotten who they are`() =
        runTest {
            userRepository.biometricEnabledUser = enrolled
            userRepository.remoteUser = enrolled

            useCase()

            assertEquals(listOf("user-1"), sessionManager.ensureValidSessionCalls)
        }

    @Test
    fun `fresh session — succeeds with the re-read remote profile`() = runTest {
        userRepository.biometricEnabledUser = enrolled
        userRepository.remoteUser = enrolled.copy(name = "Carlos V. Rojas")
        sessionManager.sessionResult = SessionResult.VALID

        val result = useCase() as BiometricLoginResult.Success

        assertEquals("Carlos V. Rojas", result.user.name)
        assertTrue(result.isFreshSession)
    }

    @Test
    fun `fresh session — remote row missing — falls back to the cached profile`() = runTest {
        userRepository.biometricEnabledUser = enrolled
        userRepository.remoteUser = null
        sessionManager.sessionResult = SessionResult.VALID

        val result = useCase() as BiometricLoginResult.Success

        assertEquals(enrolled, result.user)
        assertTrue(result.isFreshSession)
    }

    @Test
    fun `fresh session — account deactivated remotely — is refused and loses its enrolment`() = runTest {
        userRepository.biometricEnabledUser = enrolled
        userRepository.remoteUser = enrolled.copy(isActive = false)
        sessionManager.sessionResult = SessionResult.VALID

        val result = useCase()

        assertEquals(BiometricLoginResult.AccountDisabled, result)
        assertEquals(1, sessionManager.forgetDeviceCount)
        assertEquals(listOf("user-1" to null), userRepository.biometricUpdates)
    }

    @Test
    fun `offline — logs in from the local cache without a fresh session`() = runTest {
        userRepository.biometricEnabledUser = enrolled
        sessionManager.sessionResult = SessionResult.OFFLINE

        val result = useCase() as BiometricLoginResult.Success

        assertEquals(enrolled, result.user)
        assertEquals(false, result.isFreshSession)
        assertEquals(0, userRepository.syncFromRemoteCount)
    }

    @Test
    fun `deferred — startup still settling — also logs in from the cache`() = runTest {
        userRepository.biometricEnabledUser = enrolled
        sessionManager.sessionResult = SessionResult.DEFERRED

        val result = useCase() as BiometricLoginResult.Success

        assertEquals(false, result.isFreshSession)
    }

    @Test
    fun `offline — cached account already inactive — is refused but keeps its enrolment`() = runTest {
        userRepository.biometricEnabledUser = enrolled.copy(isActive = false)
        sessionManager.sessionResult = SessionResult.OFFLINE

        val result = useCase()

        assertEquals(BiometricLoginResult.AccountDisabled, result)
        // Nothing was verified against the server, so the enrolment must survive.
        assertEquals(0, sessionManager.forgetDeviceCount)
        assertTrue(userRepository.biometricUpdates.isEmpty())
    }

    @Test
    fun `revoked token — demands a password login`() = runTest {
        userRepository.biometricEnabledUser = enrolled
        sessionManager.sessionResult = SessionResult.REVOKED

        assertEquals(BiometricLoginResult.PasswordRequired, useCase())
        assertEquals(0, userRepository.syncFromRemoteCount)
    }
}
