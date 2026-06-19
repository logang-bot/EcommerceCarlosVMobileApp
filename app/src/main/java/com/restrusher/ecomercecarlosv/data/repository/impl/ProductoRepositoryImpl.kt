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

    override val isSyncing: Flow<Boolean> = dataSynchronizer.isSyncingEntity(EntityType.PRODUCTO)

    override fun getAll(): Flow<List<Producto>> {
        dataSynchronizer.triggerSyncIfStale(EntityType.PRODUCTO, DataSynchronizer.THRESHOLD_MASTER_MS)
        return dao.getAll().map { it.map(ProductoMapper::toDomain) }
    }

    override suspend fun refresh(): Boolean = dataSynchronizer.forceSync(EntityType.PRODUCTO)

    override suspend fun getById(id: String): Producto? =
        dao.getById(id)?.let(ProductoMapper::toDomain)

    override suspend fun save(producto: Producto) {
        dao.insert(ProductoMapper.toEntity(producto))
        enqueue(SyncOp.UPSERT, producto.id, producto.name)
    }

    override suspend fun delete(id: String) {
        val label = dao.getById(id)?.name ?: ""
        dao.softDeleteById(id)
        enqueue(SyncOp.DELETE, id, label)
    }

    private suspend fun enqueue(operation: String, entityId: String, label: String) {
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.PRODUCTO,
                entityId = entityId,
                operation = operation,
                entityLabel = label,
            ),
        )
    }
}
