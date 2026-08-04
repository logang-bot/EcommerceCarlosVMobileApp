package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository : UserRepository {

    private val users = MutableStateFlow<List<AppUser>>(emptyList())

    /** The account holding the fingerprint enrolment, if any. */
    var biometricEnabledUser: AppUser? = null

    /** What [syncFromRemote] hands back; null models a row that does not exist remotely yet. */
    var remoteUser: AppUser? = null
    var syncFromRemoteCount = 0
        private set

    /** Every `(id, enabledAt)` passed to [setBiometricEnabled], in order. */
    val biometricUpdates = mutableListOf<Pair<String, Long?>>()

    fun givenUsers(vararg items: AppUser) {
        users.value = items.toList()
    }

    override fun getAll(): Flow<List<AppUser>> = users

    override suspend fun getById(id: String): AppUser? = users.value.find { it.id == id }

    override suspend fun save(user: AppUser) {
        users.value = users.value.filterNot { it.id == user.id } + user
    }

    override suspend fun delete(id: String) {
        users.value = users.value.filterNot { it.id == id }
    }

    override suspend fun setActive(id: String, active: Boolean) = Unit

    override suspend fun setBiometricEnabled(id: String, enabledAt: Long?) {
        biometricUpdates += id to enabledAt
    }

    override suspend fun clearBiometricEnabledExcept(id: String) = Unit

    override suspend fun hasBiometricEnabled(): Boolean = biometricEnabledUser != null

    override suspend fun getBiometricEnabledUser(): AppUser? = biometricEnabledUser

    override suspend fun updateProfile(
        id: String,
        name: String,
        email: String,
        phone: String?,
        photoUrl: String?,
    ) = Unit

    override suspend fun syncFromRemote(userId: String): AppUser? {
        syncFromRemoteCount++
        return remoteUser
    }

    override suspend fun syncAllFromRemote() = Unit
}
