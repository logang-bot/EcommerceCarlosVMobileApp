package com.restrusher.ecomercecarlosv.fixtures

import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus

fun pedido(
    id: String = "pedido-1",
    clienteId: String = "cliente-1",
    status: PedidoStatus = PedidoStatus.PENDING,
    total: Double = 100.0,
    paid: Double = 0.0,
    createdAt: Long = 0L,
    paidAt: Long? = null,
    isSaldoExtra: Boolean = false,
    notes: String? = null,
    itemCount: Int = 0,
): Pedido = Pedido(
    id = id,
    clienteId = clienteId,
    status = status,
    total = total,
    paid = paid,
    notes = notes,
    createdAt = createdAt,
    paidAt = paidAt,
    isSaldoExtra = isSaldoExtra,
    itemCount = itemCount,
)

fun detallePedido(
    id: String = "detalle-1",
    pedidoId: String = "pedido-1",
    productoId: String = "producto-1",
    productName: String = "Arroz",
    quantity: Int = 1,
    unitPrice: Double = 10.0,
    catalogPrice: Double = 10.0,
    notes: String? = null,
): DetallePedido = DetallePedido(
    id = id,
    pedidoId = pedidoId,
    productoId = productoId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    catalogPrice = catalogPrice,
    notes = notes,
)
