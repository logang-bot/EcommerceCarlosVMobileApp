package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.Cliente
import kotlinx.coroutines.flow.Flow

interface ClienteRepository {
    fun getAll(): Flow<List<Cliente>>
    fun getByMercado(mercadoId: String): Flow<List<Cliente>>
    fun getBlacklisted(): Flow<List<Cliente>>
    suspend fun getById(id: String): Cliente?
    suspend fun save(cliente: Cliente)
    suspend fun delete(id: String)
    suspend fun blacklist(id: String, reason: String, balance: Double, at: Long)
}
