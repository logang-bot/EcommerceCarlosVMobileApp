package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.Producto
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    fun getAll(): Flow<List<Producto>>
    suspend fun getById(id: String): Producto?
    suspend fun save(producto: Producto)
    suspend fun delete(id: String)
}
