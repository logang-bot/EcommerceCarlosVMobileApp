package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.fixtures.appUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Uses mocks rather than fakes: the whole contract here is the *order* of three calls, which
 * `coVerifyOrder` states directly and a fake could only approximate by recording a call log.
 */
class ForgetEnrolledUserUseCaseTest {

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val cleaner = mockk<DeviceDataCleaner>(relaxed = true)
    private val useCase = ForgetEnrolledUserUseCase(sessionManager, userRepository, cleaner)

    @Test
    fun `invoke — pushes and wipes before revoking, so queued writes still have a live session`() =
        runTest {
            coEvery { userRepository.getBiometricEnabledUser() } returns appUser(id = "user-1")

            useCase()

            coVerifyOrder {
                cleaner.wipeCachedDataIfFullySynced()
                sessionManager.forgetDevice()
                userRepository.setBiometricEnabled("user-1", null)
            }
        }

    @Test
    fun `invoke — clears the enrolment of the user who was enrolled`() = runTest {
        coEvery { userRepository.getBiometricEnabledUser() } returns appUser(id = "user-7")

        useCase()

        coVerify(exactly = 1) { userRepository.setBiometricEnabled("user-7", null) }
    }

    @Test
    fun `invoke — nobody enrolled — still wipes and revokes, but clears no enrolment`() = runTest {
        coEvery { userRepository.getBiometricEnabledUser() } returns null

        useCase()

        coVerify(exactly = 1) { cleaner.wipeCachedDataIfFullySynced() }
        coVerify(exactly = 1) { sessionManager.forgetDevice() }
        coVerify(exactly = 0) { userRepository.setBiometricEnabled(any(), any()) }
    }

    @Test
    fun `invoke — queue did not drain so the cache was kept — still revokes and unenrols`() = runTest {
        coEvery { userRepository.getBiometricEnabledUser() } returns appUser(id = "user-1")
        coEvery { cleaner.wipeCachedDataIfFullySynced() } returns false

        useCase()

        coVerify(exactly = 1) { sessionManager.forgetDevice() }
        coVerify(exactly = 1) { userRepository.setBiometricEnabled("user-1", null) }
    }

    @Test
    fun `invoke — never wipes the cache the way a device handover would`() = runTest {
        coEvery { userRepository.getBiometricEnabledUser() } returns appUser(id = "user-1")

        useCase()

        coVerify(exactly = 0) { cleaner.wipeCachedDataForNewUser() }
    }
}
