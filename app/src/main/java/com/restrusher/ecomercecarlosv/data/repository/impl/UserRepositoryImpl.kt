package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.data.mapper.UserMapper
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
) : UserRepository {

    override fun getAll(): Flow<List<AppUser>> =
        dao.getAll().map { it.map(UserMapper::toDomain) }

    override suspend fun getById(id: String): AppUser? =
        dao.getById(id)?.let(UserMapper::toDomain)

    override suspend fun save(user: AppUser) =
        dao.insert(UserMapper.toEntity(user))

    override suspend fun delete(id: String) =
        dao.deleteById(id)

    override suspend fun setActive(id: String, active: Boolean) =
        dao.setActive(id, active)

    override suspend fun setBiometricEnabled(id: String, enabledAt: Long?) =
        dao.setBiometricEnabled(id, enabledAt)

    override suspend fun hasBiometricEnabled(): Boolean =
        dao.countBiometricEnabled() > 0

    override suspend fun getBiometricEnabledUser(): AppUser? =
        dao.getBiometricEnabledUser()?.let(UserMapper::toDomain)

    override suspend fun updateProfile(id: String, name: String, email: String, phone: String?, photoUrl: String?) =
        dao.updateProfile(id, name, email, phone, photoUrl)
}
