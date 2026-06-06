package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.mapper.ClienteMapper
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClienteRepositoryImpl @Inject constructor(
    private val dao: ClienteDao,
) : ClienteRepository {

    override fun getAll(): Flow<List<Cliente>> =
        dao.getAll().map { it.map(ClienteMapper::toDomain) }

    override fun getByMercado(mercadoId: String): Flow<List<Cliente>> =
        dao.getByMercado(mercadoId).map { it.map(ClienteMapper::toDomain) }

    override fun getBlacklisted(): Flow<List<Cliente>> =
        dao.getBlacklisted().map { it.map(ClienteMapper::toDomain) }

    override fun getByIdFlow(id: String): Flow<Cliente?> =
        dao.getByIdFlow(id).map { it?.let(ClienteMapper::toDomain) }

    override suspend fun getById(id: String): Cliente? =
        dao.getById(id)?.let(ClienteMapper::toDomain)

    override suspend fun save(cliente: Cliente) =
        dao.insert(ClienteMapper.toEntity(cliente))

    override suspend fun delete(id: String) =
        dao.deleteById(id)

    override suspend fun blacklist(id: String, reason: String, balance: Double, at: Long) =
        dao.blacklist(id, reason, balance, at)

    override suspend fun unblacklist(id: String) =
        dao.unblacklist(id)
}
