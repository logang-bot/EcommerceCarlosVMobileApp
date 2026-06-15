package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.mapper.ClienteMapper
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClienteRepositoryImpl @Inject constructor(
    private val dao: ClienteDao,
    private val syncOperationDao: SyncOperationDao,
    private val dataSynchronizer: DataSynchronizer,
) : ClienteRepository {

    private fun triggerSync() =
        dataSynchronizer.triggerSyncIfStale(EntityType.CLIENTE, DataSynchronizer.THRESHOLD_BUSINESS_MS)

    override fun getAll(): Flow<List<Cliente>> {
        triggerSync()
        return dao.getAll().map { it.map(ClienteMapper::toDomain) }
    }

    override fun getAllIncludingBlacklisted(): Flow<List<Cliente>> {
        triggerSync()
        return dao.getAllIncludingBlacklisted().map { it.map(ClienteMapper::toDomain) }
    }

    override fun getByMercado(mercadoId: String): Flow<List<Cliente>> {
        triggerSync()
        return dao.getByMercado(mercadoId).map { it.map(ClienteMapper::toDomain) }
    }

    override fun getBlacklisted(): Flow<List<Cliente>> {
        triggerSync()
        return dao.getBlacklisted().map { it.map(ClienteMapper::toDomain) }
    }

    override fun getByIdFlow(id: String): Flow<Cliente?> =
        dao.getByIdFlow(id).map { it?.let(ClienteMapper::toDomain) }

    override suspend fun getById(id: String): Cliente? =
        dao.getById(id)?.let(ClienteMapper::toDomain)

    override suspend fun save(cliente: Cliente) {
        val entity = ClienteMapper.toEntity(cliente)
        if (dao.insert(entity) == -1L) dao.update(entity)
        enqueue(SyncOp.UPSERT, cliente.id)
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
        enqueue(SyncOp.DELETE, id)
    }

    override suspend fun blacklist(id: String, reason: String, balance: Double, at: Long, isManualAmount: Boolean) {
        dao.blacklist(id, reason, balance, at, isManualAmount)
        enqueue(SyncOp.UPSERT, id)
    }

    override suspend fun unblacklist(id: String) {
        dao.unblacklist(id)
        enqueue(SyncOp.UPSERT, id)
    }

    private suspend fun enqueue(operation: String, entityId: String) {
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.CLIENTE,
                entityId = entityId,
                operation = operation,
            ),
        )
    }
}
