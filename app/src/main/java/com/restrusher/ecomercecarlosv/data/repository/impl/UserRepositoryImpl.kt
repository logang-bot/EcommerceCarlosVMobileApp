package com.restrusher.ecomercecarlosv.data.repository.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.data.mapper.UserMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.UserDto
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val supabase: SupabaseClient,
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

    override suspend fun clearBiometricEnabledExcept(id: String) =
        dao.clearBiometricEnabledExcept(id)

    override suspend fun hasBiometricEnabled(): Boolean =
        dao.countBiometricEnabled() > 0

    override suspend fun getBiometricEnabledUser(): AppUser? =
        dao.getBiometricEnabledUser()?.let(UserMapper::toDomain)

    override suspend fun updateProfile(id: String, name: String, email: String, phone: String?, photoUrl: String?) =
        dao.updateProfile(id, name, email, phone, photoUrl)

    override suspend fun syncAllFromRemote() {
        val dtos = runCatching {
            supabase.from("users").select().decodeList<UserDto>()
        }.onFailure { e ->
            Log.e(TAG, "syncAllFromRemote: failed", e)
        }.getOrNull() ?: return

        dtos.forEach { dto ->
            val localBiometric = dao.getById(dto.id)?.biometricEnabledAt
            val entity = UserMapper.toEntity(UserMapper.toDomain(dto).copy(biometricEnabledAt = localBiometric))
            dao.insert(entity)
        }
        Log.d(TAG, "syncAllFromRemote: upserted ${dtos.size} users")
    }

    override suspend fun syncFromRemote(userId: String): AppUser? {
        val dto = runCatching {
            supabase.from("users")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserDto>()
        }.onFailure { e ->
            Log.e(TAG, "syncFromRemote: failed to fetch user $userId", e)
        }.getOrNull() ?: return null

        Log.d(TAG, "syncFromRemote: fetched user — name=${dto.name}, role=${dto.role}, isActive=${dto.isActive}")
        val localBiometric = dao.getById(userId)?.biometricEnabledAt
        val domain = UserMapper.toDomain(dto).copy(biometricEnabledAt = localBiometric)
        dao.insert(UserMapper.toEntity(domain))
        return domain
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}
