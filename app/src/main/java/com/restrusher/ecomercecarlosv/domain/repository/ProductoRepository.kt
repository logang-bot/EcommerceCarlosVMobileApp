package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.Producto
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    val isSyncing: Flow<Boolean>
    fun getAll(): Flow<List<Producto>>
    suspend fun getById(id: String): Producto?
    suspend fun save(producto: Producto)
    suspend fun delete(id: String)
    suspend fun refresh(): Boolean
}
