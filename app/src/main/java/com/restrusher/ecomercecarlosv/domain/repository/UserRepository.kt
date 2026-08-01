package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAll(): Flow<List<AppUser>>
    suspend fun getById(id: String): AppUser?
    suspend fun save(user: AppUser)
    suspend fun delete(id: String)
    suspend fun setActive(id: String, active: Boolean)
    suspend fun setBiometricEnabled(id: String, enabledAt: Long?)
    /** Drops any other account's fingerprint enrolment, so only [id] can unlock this device. */
    suspend fun clearBiometricEnabledExcept(id: String)
    suspend fun hasBiometricEnabled(): Boolean
    suspend fun getBiometricEnabledUser(): AppUser?
    suspend fun updateProfile(id: String, name: String, email: String, phone: String?, photoUrl: String?)
    /** Fetches the user row from Supabase by [userId], saves it to Room, and returns it.
     *  Returns null if the remote row doesn't exist yet. */
    suspend fun syncFromRemote(userId: String): AppUser?

    /** Fetches all users from Supabase and upserts them into Room. */
    suspend fun syncAllFromRemote()
}
