package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.fakes.FakeDeviceDataCleaner
import com.restrusher.ecomercecarlosv.fakes.FakeUserRepository
import com.restrusher.ecomercecarlosv.fixtures.appUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveDeviceHandoverUseCaseTest {

    private val userRepository = FakeUserRepository()
    private val cleaner = FakeDeviceDataCleaner()
    private val useCase = ResolveDeviceHandoverUseCase(userRepository, cleaner)

    private val incoming = appUser(id = "user-nuevo", name = "Ana Quispe")

    @Test
    fun `no previous owner — proceeds without touching the cached data`() = runTest {
        val result = useCase(incoming, previousOwnerId = null)

        assertEquals(DeviceHandover.Proceed, result)
        assertEquals(0, cleaner.wipeForNewUserCount)
    }

    @Test
    fun `same user signing back in — proceeds and keeps their cache`() = runTest {
        cleaner.pendingWrites = 5

        val result = useCase(incoming, previousOwnerId = incoming.id)

        assertEquals(DeviceHandover.Proceed, result)
        assertEquals(0, cleaner.wipeForNewUserCount)
    }

    @Test
    fun `different user with nothing queued — wipes silently and proceeds`() = runTest {
        cleaner.pendingWrites = 0

        val result = useCase(incoming, previousOwnerId = "user-anterior")

        assertEquals(DeviceHandover.Proceed, result)
        assertEquals(1, cleaner.wipeForNewUserCount)
    }

    @Test
    fun `different user with queued writes — asks for confirmation instead of wiping`() = runTest {
        userRepository.givenUsers(appUser(id = "user-anterior", name = "Carlos Vargas"))
        cleaner.pendingWrites = 3

        val result = useCase(incoming, previousOwnerId = "user-anterior")

        assertEquals(
            DeviceHandover.ConfirmationRequired(
                incomingUserName = "Ana Quispe",
                previousUserName = "Carlos Vargas",
                pendingCount = 3,
            ),
            result,
        )
        assertEquals(0, cleaner.wipeForNewUserCount)
    }

    @Test
    fun `previous owner no longer in the local cache — confirmation still asked, with a blank name`() =
        runTest {
            cleaner.pendingWrites = 2

            val result = useCase(incoming, previousOwnerId = "user-desconocido")

            assertEquals("", (result as DeviceHandover.ConfirmationRequired).previousUserName)
            assertEquals(2, result.pendingCount)
        }

    @Test
    fun `a single queued write is enough to require confirmation`() = runTest {
        cleaner.pendingWrites = 1

        val result = useCase(incoming, previousOwnerId = "user-anterior")

        assertEquals(1, (result as DeviceHandover.ConfirmationRequired).pendingCount)
    }
}
