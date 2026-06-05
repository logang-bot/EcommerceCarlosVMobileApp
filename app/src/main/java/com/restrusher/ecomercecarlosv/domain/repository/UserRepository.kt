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
    suspend fun hasBiometricEnabled(): Boolean
}
