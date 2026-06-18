package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.mapper.DetallePedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PedidoMapper
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PedidoRepositoryImpl @Inject constructor(
    private val pedidoDao: PedidoDao,
    private val detallePedidoDao: DetallePedidoDao,
    private val syncOperationDao: SyncOperationDao,
    private val dataSynchronizer: DataSynchronizer,
) : PedidoRepository {

    override val isSyncing: Flow<Boolean> = dataSynchronizer.isSyncingEntity(EntityType.PEDIDO)

    override suspend fun refresh(): Boolean = dataSynchronizer.forceSync(EntityType.PEDIDO)

    private fun triggerSync() =
        dataSynchronizer.triggerSyncIfStale(EntityType.PEDIDO, DataSynchronizer.THRESHOLD_BUSINESS_MS)

    override fun getByCliente(clienteId: String): Flow<List<Pedido>> {
        triggerSync()
        return pedidoDao.getByCliente(clienteId).map { it.map(PedidoMapper::toDomain) }
    }

    override fun getByClienteWithLines(clienteId: String): Flow<List<Pedido>> {
        triggerSync()
        return pedidoDao.getByClienteWithLines(clienteId).map { it.map(PedidoMapper::toDomain) }
    }

    override fun getAll(): Flow<List<Pedido>> {
        triggerSync()
        return pedidoDao.getAll().map { it.map(PedidoMapper::toDomain) }
    }

    override fun getAllUnpaid(): Flow<List<Pedido>> {
        triggerSync()
        return pedidoDao.getAllUnpaid().map { it.map(PedidoMapper::toDomain) }
    }

    override fun getByIdFlow(id: String): Flow<Pedido?> =
        pedidoDao.getByIdFlow(id).map { it?.let(PedidoMapper::toDomain) }

    override suspend fun getById(id: String): Pedido? =
        pedidoDao.getById(id)?.let(PedidoMapper::toDomain)

    override suspend fun getDetallesByPedido(pedidoId: String): List<DetallePedido> =
        detallePedidoDao.getByPedido(pedidoId).map(DetallePedidoMapper::toDomain)

    override fun getDetallesByPedidoFlow(pedidoId: String): Flow<List<DetallePedido>> =
        detallePedidoDao.getByPedidoFlow(pedidoId).map { it.map(DetallePedidoMapper::toDomain) }

    override suspend fun create(pedido: Pedido, detalles: List<DetallePedido>) {
        pedidoDao.insert(PedidoMapper.toEntity(pedido))
        detallePedidoDao.insertAll(detalles.map(DetallePedidoMapper::toEntity))
        enqueue(SyncOp.UPSERT, pedido.id, "Bs. ${formatPedidoAmount(pedido.total)}")
    }

    override suspend fun updateStatus(id: String, status: PedidoStatus, paid: Double, paidAt: Long?) {
        val label = pedidoDao.getById(id)?.let { "Bs. ${formatPedidoAmount(it.total)}" } ?: ""
        pedidoDao.updateStatus(id, status.name, paid, paidAt)
        enqueue(SyncOp.UPSERT, id, label)
    }

    override suspend fun updateDate(id: String, createdAt: Long) {
        val label = pedidoDao.getById(id)?.let { "Bs. ${formatPedidoAmount(it.total)}" } ?: ""
        pedidoDao.updateDate(id, createdAt)
        enqueue(SyncOp.UPSERT, id, label)
    }

    override suspend fun updateLines(pedidoId: String, detalles: List<DetallePedido>, newTotal: Double, paid: Double, paidAt: Long?) {
        val newStatus = when {
            paid >= newTotal -> PedidoStatus.PAID
            paid > 0        -> PedidoStatus.PARTIAL
            else            -> PedidoStatus.PENDING
        }
        detallePedidoDao.deleteByPedido(pedidoId)
        detallePedidoDao.insertAll(detalles.map(DetallePedidoMapper::toEntity))
        pedidoDao.updateAfterEdit(pedidoId, newTotal, detalles.size, newStatus.name, paid, paidAt)
        enqueue(SyncOp.UPSERT, pedidoId, "Bs. ${formatPedidoAmount(newTotal)}")
    }

    override suspend fun delete(id: String) {
        val label = pedidoDao.getById(id)?.let { "Bs. ${formatPedidoAmount(it.total)}" } ?: ""
        pedidoDao.deleteById(id)
        enqueue(SyncOp.DELETE, id, label)
    }

    override suspend fun markAllPaidForCliente(clienteId: String) =
        pedidoDao.markAllPaidForCliente(clienteId, System.currentTimeMillis())

    private fun formatPedidoAmount(amount: Double): String =
        String.format("%.2f", amount).replace('.', ',')

    private suspend fun enqueue(operation: String, entityId: String, label: String) {
        syncOperationDao.enqueue(
            SyncOperationEntity(
                entityType = EntityType.PEDIDO,
                entityId = entityId,
                operation = operation,
                entityLabel = label,
            ),
        )
    }
}
