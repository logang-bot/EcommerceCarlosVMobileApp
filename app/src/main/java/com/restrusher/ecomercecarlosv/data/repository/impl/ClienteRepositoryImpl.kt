package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
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
    private val pedidoDao: PedidoDao,
    private val syncOperationDao: SyncOperationDao,
    private val dataSynchronizer: DataSynchronizer,
) : ClienteRepository {

    override val isSyncing: Flow<Boolean> = dataSynchronizer.isSyncingEntity(EntityType.CLIENTE)

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

    override fun countByMercado(mercadoId: String): Flow<Int> = dao.countByMercado(mercadoId)

    override suspend fun refresh(): Boolean = dataSynchronizer.forceSync(EntityType.CLIENTE)

    override fun getByIdFlow(id: String): Flow<Cliente?> =
        dao.getByIdFlow(id).map { it?.let(ClienteMapper::toDomain) }

    override suspend fun getById(id: String): Cliente? =
        dao.getById(id)?.let(ClienteMapper::toDomain)

    override suspend fun save(cliente: Cliente) {
        val entity = ClienteMapper.toEntity(cliente)
        if (dao.insert(entity) == -1L) dao.update(entity)
        enqueue(SyncOp.UPSERT, cliente.id, cliente.name)
    }

    /**
     * Soft-deletes the cliente and its pedidos. Only the cliente is enqueued: the Supabase
     * `trg_clientes_cascade_soft_delete` trigger flips the pedidos server-side, so per-pedido ops
     * would be redundant and would flood the queue. The local writes just spare this device the wait
     * for the next delta.
     */
    override suspend fun delete(id: String) {
        val label = dao.getById(id)?.name ?: ""
        pedidoDao.softDeleteByCliente(id)
        dao.softDeleteById(id)
        enqueue(SyncOp.DELETE, id, label)
    }

    override suspend fun blacklist(id: String, reason: String, balance: Double, at: Long, isManualAmount: Boolean) {
        val label = dao.getById(id)?.name ?: ""
        dao.blacklist(id, reason, balance, at, isManualAmount)
        enqueue(SyncOp.UPSERT, id, label)
    }

    override suspend fun unblacklist(id: String) {
        val label = dao.getById(id)?.name ?: ""
        dao.unblacklist(id)
        enqueue(SyncOp.UPSERT, id, label)
    }

    private suspend fun enqueue(operation: String, entityId: String, label: String) {
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.CLIENTE,
                entityId = entityId,
                operation = operation,
                entityLabel = label,
            ),
        )
    }
}
