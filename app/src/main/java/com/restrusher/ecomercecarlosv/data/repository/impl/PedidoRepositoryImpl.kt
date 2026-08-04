package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PagoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.data.mapper.DetallePedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PagoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PedidoMapper
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pago
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class PedidoRepositoryImpl @Inject constructor(
    private val pedidoDao: PedidoDao,
    private val detallePedidoDao: DetallePedidoDao,
    private val pagoDao: PagoDao,
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

    override fun getPagosByPedidoFlow(pedidoId: String): Flow<List<Pago>> =
        pagoDao.getByPedidoFlow(pedidoId).map { it.map(PagoMapper::toDomain) }

    override fun getPagosByClienteFlow(clienteId: String): Flow<List<Pago>> =
        pagoDao.getByClienteFlow(clienteId).map { it.map(PagoMapper::toDomain) }

    override suspend fun create(pedido: Pedido, detalles: List<DetallePedido>) {
        pedidoDao.insert(PedidoMapper.toEntity(pedido))
        detallePedidoDao.insertAll(detalles.map(DetallePedidoMapper::toEntity))
        if (pedido.paid > 0) pagoDao.insert(initialPagoFor(pedido))
        enqueue(SyncOp.UPSERT, pedido.id, "Bs. ${formatPedidoAmount(pedido.total)}")
    }

    private fun initialPagoFor(pedido: Pedido) = PagoMapper.toEntity(
        Pago(
            id = UUID.randomUUID().toString(),
            pedidoId = pedido.id,
            amount = pedido.paid,
            paidAt = pedido.paidAt ?: pedido.createdAt,
        ),
    )

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
        val oldPaid = pedidoDao.getById(pedidoId)?.paid ?: 0.0
        val newStatus = when {
            paid >= newTotal -> PedidoStatus.PAID
            paid > 0        -> PedidoStatus.PARTIAL
            else            -> PedidoStatus.PENDING
        }
        detallePedidoDao.deleteByPedido(pedidoId)
        detallePedidoDao.insertAll(detalles.map(DetallePedidoMapper::toEntity))
        pedidoDao.updateAfterEdit(pedidoId, newTotal, detalles.size, newStatus.name, paid, paidAt)
        val delta = paid - oldPaid
        if (delta > 0) pagoDao.insert(deltaPagoFor(pedidoId, delta, paidAt))
        enqueue(SyncOp.UPSERT, pedidoId, "Bs. ${formatPedidoAmount(newTotal)}")
    }

    private fun deltaPagoFor(pedidoId: String, amount: Double, paidAt: Long?) = PagoMapper.toEntity(
        Pago(
            id = UUID.randomUUID().toString(),
            pedidoId = pedidoId,
            amount = amount,
            paidAt = paidAt ?: System.currentTimeMillis(),
        ),
    )

    override suspend fun registrarPago(pago: Pago, newPaid: Double, newStatus: PedidoStatus) {
        pagoDao.insert(PagoMapper.toEntity(pago))
        pedidoDao.updateStatus(pago.pedidoId, newStatus.name, newPaid, pago.paidAt)
        enqueue(SyncOp.UPSERT, pago.pedidoId, "Bs. ${formatPedidoAmount(newPaid)}")
    }

    override suspend fun delete(id: String) {
        val label = pedidoDao.getById(id)?.let { "Bs. ${formatPedidoAmount(it.total)}" } ?: ""
        pedidoDao.softDeleteById(id)
        enqueue(SyncOp.DELETE, id, label)
    }

    override suspend fun markAllPaidForCliente(clienteId: String) {
        // Read first: after the update the same predicate matches nothing. The bulk UPDATE has no
        // single entity id, and QueueProcessor keys on (entityType, entityId), so it takes one
        // queue row per settled pedido rather than one for the whole operation.
        val settled = pedidoDao.unpaidForCliente(clienteId)
        pedidoDao.markAllPaidForCliente(clienteId, System.currentTimeMillis())
        settled.forEach { enqueue(SyncOp.UPSERT, it.id, "Bs. ${formatPedidoAmount(it.total)}") }
    }

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
