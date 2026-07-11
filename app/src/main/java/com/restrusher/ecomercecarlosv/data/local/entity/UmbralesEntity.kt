package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton row — there is exactly one, always keyed by [SINGLETON_ID]. Superusuario-managed
 * business config, not a per-user preference, so it's synced to Supabase like every other
 * shared table rather than kept in local SharedPreferences.
 */
@Entity(tableName = "umbrales")
data class UmbralesEntity(
    @PrimaryKey val id: String = SINGLETON_ID,
    val montoMaximo: Double,
    val diasMaximos: Int,
    val updatedAt: Long = 0L,
) {
    companion object {
        const val SINGLETON_ID = "global"
    }
}
