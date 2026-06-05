package com.restrusher.ecomercecarlosv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE users SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean)

    @Query("UPDATE users SET biometricEnabledAt = :enabledAt WHERE id = :id")
    suspend fun setBiometricEnabled(id: String, enabledAt: Long?)

    @Query("SELECT COUNT(*) FROM users WHERE biometricEnabledAt IS NOT NULL")
    suspend fun countBiometricEnabled(): Int

    @Query("SELECT * FROM users WHERE biometricEnabledAt IS NOT NULL LIMIT 1")
    suspend fun getBiometricEnabledUser(): UserEntity?

    @Query("UPDATE users SET name = :name, email = :email, phone = :phone WHERE id = :id")
    suspend fun updateProfile(id: String, name: String, email: String, phone: String?)
}
