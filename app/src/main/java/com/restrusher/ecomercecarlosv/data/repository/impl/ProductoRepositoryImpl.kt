package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.mapper.ProductoMapper
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.Producto
import com.restrusher.ecomercecarlosv.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductoRepositoryImpl @Inject constructor(
    private val dao: ProductoDao,
    private val syncOperationDao: SyncOperationDao,
    private val dataSynchronizer: DataSynchronizer,
) : ProductoRepository {

    override fun getAll(): Flow<List<Producto>> {
        dataSynchronizer.triggerSyncIfStale(EntityType.PRODUCTO, DataSynchronizer.THRESHOLD_MASTER_MS)
        return dao.getAll().map { it.map(ProductoMapper::toDomain) }
    }

    override suspend fun getById(id: String): Producto? =
        dao.getById(id)?.let(ProductoMapper::toDomain)

    override suspend fun save(producto: Producto) {
        dao.insert(ProductoMapper.toEntity(producto))
        enqueue(SyncOp.UPSERT, producto.id)
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
        enqueue(SyncOp.DELETE, id)
    }

    private suspend fun enqueue(operation: String, entityId: String) {
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.PRODUCTO,
                entityId = entityId,
                operation = operation,
            ),
        )
    }
}
