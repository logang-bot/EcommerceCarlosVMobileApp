package com.restrusher.ecomercecarlosv.domain.repository

import com.restrusher.ecomercecarlosv.domain.model.Mercado
import kotlinx.coroutines.flow.Flow

interface MercadoRepository {
    fun getAll(): Flow<List<Mercado>>
    fun getByIdFlow(id: String): Flow<Mercado?>
    suspend fun getById(id: String): Mercado?
    suspend fun save(mercado: Mercado)
    suspend fun delete(id: String)
}
