package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner
import javax.inject.Inject

/** What a login must do about the data already cached on this device. */
sealed interface DeviceHandover {
    /** Same user as before, or nothing was at risk and the cache has already been cleared. */
    data object Proceed : DeviceHandover

    /** A different user is signing in over unsynced work. Only they can decide to discard it. */
    data class ConfirmationRequired(
        val incomingUserName: String,
        val previousUserName: String,
        val pendingCount: Int,
    ) : DeviceHandover
}

/**
 * Decides whether a freshly authenticated user may inherit this device.
 *
 * Queued writes carry no author of their own, and RLS only checks the signed-in user's *role* — so
 * flushing one user's queue under another's session files their pedidos under the wrong account,
 * silently and successfully. The cached tables leak the same way, just visibly. Both are settled
 * here, before the login is allowed to complete.
 *
 * @param previousOwnerId must be read *before* authenticating: signing in makes the incoming user
 *   the active session immediately, which would otherwise erase the very difference being detected.
 */
class ResolveDeviceHandoverUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val deviceDataCleaner: DeviceDataCleaner,
) {
    suspend operator fun invoke(incomingUser: AppUser, previousOwnerId: String?): DeviceHandover {
        if (previousOwnerId == null || previousOwnerId == incomingUser.id) return DeviceHandover.Proceed
        val pending = deviceDataCleaner.pendingWriteCount()
        if (pending > 0) return confirmationFor(incomingUser, previousOwnerId, pending)
        // Everything reached the server already, so there is nothing to weigh — just make sure the
        // incoming user does not open the app onto somebody else's mercados and clientes.
        deviceDataCleaner.wipeCachedDataForNewUser()
        return DeviceHandover.Proceed
    }

    private suspend fun confirmationFor(
        incomingUser: AppUser,
        previousOwnerId: String,
        pending: Int,
    ): DeviceHandover = DeviceHandover.ConfirmationRequired(
        incomingUserName = incomingUser.name,
        previousUserName = userRepository.getById(previousOwnerId)?.name.orEmpty(),
        pendingCount = pending,
    )
}
