package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import kotlinx.coroutines.flow.Flow

interface UmbralesRepository {
    val isSyncing: Flow<Boolean>
    fun getUmbrales(): Flow<Umbrales>
    suspend fun save(umbrales: Umbrales)
    suspend fun refresh(): Boolean
}
