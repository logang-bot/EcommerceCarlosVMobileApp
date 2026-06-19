package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.mapper.MercadoMapper
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MercadoRepositoryImpl @Inject constructor(
    private val dao: MercadoDao,
    private val syncOperationDao: SyncOperationDao,
    private val dataSynchronizer: DataSynchronizer,
) : MercadoRepository {

    override val isSyncing: Flow<Boolean> = dataSynchronizer.isSyncingEntity(EntityType.MERCADO)

    override fun getAll(): Flow<List<Mercado>> {
        dataSynchronizer.triggerSyncIfStale(EntityType.MERCADO, DataSynchronizer.THRESHOLD_MASTER_MS)
        return dao.getAll().map { it.map(MercadoMapper::toDomain) }
    }

    override suspend fun refresh(): Boolean = dataSynchronizer.forceSync(EntityType.MERCADO)

    override fun getByIdFlow(id: String): Flow<Mercado?> =
        dao.getByIdFlow(id).map { it?.let(MercadoMapper::toDomain) }

    override suspend fun getById(id: String): Mercado? =
        dao.getById(id)?.let(MercadoMapper::toDomain)

    override suspend fun save(mercado: Mercado) {
        val entity = MercadoMapper.toEntity(mercado)
        if (dao.insert(entity) == -1L) dao.update(entity)
        enqueue(SyncOp.UPSERT, mercado.id, mercado.name)
    }

    override suspend fun delete(id: String) {
        val label = dao.getById(id)?.name ?: ""
        dao.softDeleteById(id)
        enqueue(SyncOp.DELETE, id, label)
    }

    private suspend fun enqueue(operation: String, entityId: String, label: String) {
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.MERCADO,
                entityId = entityId,
                operation = operation,
                entityLabel = label,
            ),
        )
    }
}
