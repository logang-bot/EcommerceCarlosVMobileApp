package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.mapper.ProductoMapper
import com.restrusher.ecomercecarlosv.domain.model.Producto
import com.restrusher.ecomercecarlosv.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductoRepositoryImpl @Inject constructor(
    private val dao: ProductoDao,
) : ProductoRepository {

    override fun getAll(): Flow<List<Producto>> =
        dao.getAll().map { it.map(ProductoMapper::toDomain) }

    override suspend fun getById(id: String): Producto? =
        dao.getById(id)?.let(ProductoMapper::toDomain)

    override suspend fun save(producto: Producto) =
        dao.insert(ProductoMapper.toEntity(producto))

    override suspend fun delete(id: String) =
        dao.deleteById(id)
}
