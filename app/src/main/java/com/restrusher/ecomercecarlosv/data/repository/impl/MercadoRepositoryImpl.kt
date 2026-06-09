package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.mapper.MercadoMapper
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MercadoRepositoryImpl @Inject constructor(
    private val dao: MercadoDao,
) : MercadoRepository {

    override fun getAll(): Flow<List<Mercado>> =
        dao.getAll().map { it.map(MercadoMapper::toDomain) }

    override fun getByIdFlow(id: String): Flow<Mercado?> =
        dao.getByIdFlow(id).map { it?.let(MercadoMapper::toDomain) }

    override suspend fun getById(id: String): Mercado? =
        dao.getById(id)?.let(MercadoMapper::toDomain)

    override suspend fun save(mercado: Mercado) {
        val entity = MercadoMapper.toEntity(mercado)
        if (dao.insert(entity) == -1L) dao.update(entity)
    }

    override suspend fun delete(id: String) =
        dao.deleteById(id)
}
