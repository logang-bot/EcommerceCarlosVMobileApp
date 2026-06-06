package com.restrusher.ecomercecarlosv.data.repository.impl

import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.mapper.DetallePedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PedidoMapper
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
) : PedidoRepository {

    override fun getByCliente(clienteId: String): Flow<List<Pedido>> =
        pedidoDao.getByCliente(clienteId).map { it.map(PedidoMapper::toDomain) }

    override fun getAllUnpaid(): Flow<List<Pedido>> =
        pedidoDao.getAllUnpaid().map { it.map(PedidoMapper::toDomain) }

    override fun getByIdFlow(id: String): Flow<Pedido?> =
        pedidoDao.getByIdFlow(id).map { it?.let(PedidoMapper::toDomain) }

    override suspend fun getById(id: String): Pedido? =
        pedidoDao.getById(id)?.let(PedidoMapper::toDomain)

    override suspend fun getDetallesByPedido(pedidoId: String): List<DetallePedido> =
        detallePedidoDao.getByPedido(pedidoId).map(DetallePedidoMapper::toDomain)

    override suspend fun create(pedido: Pedido, detalles: List<DetallePedido>) {
        pedidoDao.insert(PedidoMapper.toEntity(pedido))
        detallePedidoDao.insertAll(detalles.map(DetallePedidoMapper::toEntity))
    }

    override suspend fun updateStatus(id: String, status: PedidoStatus, paid: Double, paidAt: Long?) {
        pedidoDao.updateStatus(id, status.name, paid, paidAt)
    }

    override suspend fun delete(id: String) = pedidoDao.deleteById(id)
}
