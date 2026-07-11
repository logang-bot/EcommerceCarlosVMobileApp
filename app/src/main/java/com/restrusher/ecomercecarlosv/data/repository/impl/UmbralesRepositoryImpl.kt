package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.dao.UmbralesDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import com.restrusher.ecomercecarlosv.data.mapper.UmbralesMapper
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UmbralesRepositoryImpl @Inject constructor(
    private val dao: UmbralesDao,
    private val syncOperationDao: SyncOperationDao,
    private val dataSynchronizer: DataSynchronizer,
) : UmbralesRepository {

    override val isSyncing: Flow<Boolean> = dataSynchronizer.isSyncingEntity(EntityType.UMBRALES)

    override fun getUmbrales(): Flow<Umbrales> {
        dataSynchronizer.triggerSyncIfStale(EntityType.UMBRALES, DataSynchronizer.THRESHOLD_MASTER_MS)
        return dao.getFlow().map { it?.let(UmbralesMapper::toDomain) ?: Umbrales() }
    }

    override suspend fun refresh(): Boolean = dataSynchronizer.forceSync(EntityType.UMBRALES)

    override suspend fun save(umbrales: Umbrales) {
        dao.insert(UmbralesMapper.toEntity(umbrales))
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.UMBRALES,
                entityId = UmbralesEntity.SINGLETON_ID,
                operation = SyncOp.UPSERT,
                entityLabel = "Umbrales de estado",
            ),
        )
    }
}
