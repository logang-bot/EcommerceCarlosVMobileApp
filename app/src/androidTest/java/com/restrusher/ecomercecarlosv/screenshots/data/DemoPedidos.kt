package com.restrusher.ecomercecarlosv.screenshots.data

import com.restrusher.ecomercecarlosv.data.local.entity.DetallePedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PagoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import com.restrusher.ecomercecarlosv.screenshots.data.DemoClock.daysAgo
import com.restrusher.ecomercecarlosv.screenshots.data.DemoIds as Id

/** [paidShare] 0 makes the pedido PENDING, 1 makes it PAID, anything between makes it PARTIAL. */
data class DemoPedido(
    val id: String,
    val clienteId: String,
    val daysAgo: Int,
    val paidShare: Double,
    val lines: List<Pair<String, Int>>,
)

/** A pedido with its lines and, when something was paid, the single pago that paid it. */
class SeededPedido(val pedido: PedidoEntity, val lines: List<DetallePedidoEntity>, val pago: PagoEntity?)

/**
 * The pedidos behind every balance in the demo. Rosa Quispe is the cliente the detail screens show,
 * so she gets the widest mix: partial, paid, pending and a saldo extra. Juan (old partial) and
 * Teresa (partial above the Bs. 500 umbral) go critical; Rosa stays in the warning band.
 */
object DemoPedidos {

    val specs = listOf(
        DemoPedido(Id.PEDIDO_ROSA_PARCIAL, Id.CLIENTE_ROSA, 2, 0.4,
            listOf(Id.PRODUCTO_ARROZ to 8, Id.PRODUCTO_ACEITE to 6, Id.PRODUCTO_AZUCAR to 10, Id.PRODUCTO_LECHE to 12)),
        DemoPedido("pedido-rosa-2", Id.CLIENTE_ROSA, 9, 1.0,
            listOf(Id.PRODUCTO_HARINA to 10, Id.PRODUCTO_FIDEO to 15, Id.PRODUCTO_HUEVOS to 2)),
        DemoPedido("pedido-rosa-3", Id.CLIENTE_ROSA, 1, 0.0, listOf(Id.PRODUCTO_CAFE to 3, Id.PRODUCTO_ARROZ to 4)),
        DemoPedido("pedido-rosa-4", Id.CLIENTE_ROSA, 24, 1.0, listOf(Id.PRODUCTO_ACEITE to 10, Id.PRODUCTO_AZUCAR to 10)),
        DemoPedido("pedido-juan-1", "cliente-juan", 45, 0.5, listOf(Id.PRODUCTO_HUEVOS to 4, Id.PRODUCTO_ARROZ to 6)),
        DemoPedido("pedido-carmen-1", "cliente-carmen", 5, 1.0, listOf(Id.PRODUCTO_HARINA to 6)),
        DemoPedido("pedido-luis-1", "cliente-luis", 1, 0.0, listOf(Id.PRODUCTO_FIDEO to 10, Id.PRODUCTO_LECHE to 6)),
        DemoPedido("pedido-teresa-1", "cliente-teresa", 3, 0.2, listOf(Id.PRODUCTO_CAFE to 20, Id.PRODUCTO_ACEITE to 10)),
        DemoPedido("pedido-pedro-1", "cliente-pedro", 60, 0.25, listOf(Id.PRODUCTO_HUEVOS to 5)),
        DemoPedido("pedido-ana-1", "cliente-ana", 4, 0.5, listOf(Id.PRODUCTO_ARROZ to 10)),
        DemoPedido("pedido-miguel-1", "cliente-miguel", 40, 0.3, listOf(Id.PRODUCTO_ACEITE to 15)),
        DemoPedido("pedido-sofia-1", "cliente-sofia", 2, 1.0, listOf(Id.PRODUCTO_LECHE to 10)),
        DemoPedido("pedido-jorge-1", "cliente-jorge", 6, 0.6, listOf(Id.PRODUCTO_CAFE to 5)),
        DemoPedido("pedido-elena-1", "cliente-elena", 1, 0.0, listOf(Id.PRODUCTO_HUEVOS to 3)),
        DemoPedido("pedido-raul-1", "cliente-raul", 8, 1.0, listOf(Id.PRODUCTO_AZUCAR to 8)),
    )

    /** Debt recorded by hand rather than delivered: no lines, never counts towards the status colour. */
    val rosaSaldoExtra = PedidoEntity(
        id = "pedido-rosa-saldo", clienteId = Id.CLIENTE_ROSA, status = "PENDING", total = 80.0, paid = 0.0,
        notes = "Saldo pendiente de la temporada anterior", createdAt = daysAgo(15), paidAt = null,
        isSaldoExtra = true,
    )

    fun seeded(): List<SeededPedido> = specs.map { it.toSeeded() }

    fun createdAt(pedidoId: String): Long = DemoClock.daysAgo(specs.first { it.id == pedidoId }.daysAgo)

    private fun DemoPedido.toSeeded(): SeededPedido {
        val detalles = lines.mapIndexed { index, item -> line(this, index, item) }
        val total = detalles.sumOf { it.unitPrice * it.quantity }
        val paid = total * paidShare
        // Paid the day after delivery, or today for a pedido delivered today.
        val paidAt = DemoClock.daysAgo(maxOf(daysAgo - 1, 0)).takeIf { paidShare > 0.0 }
        val pedido = PedidoEntity(
            id = id, clienteId = clienteId, status = statusFor(paidShare), total = total, paid = paid,
            notes = null, createdAt = DemoClock.daysAgo(daysAgo), paidAt = paidAt.takeIf { paidShare >= 1.0 },
            itemCount = detalles.size,
        )
        val pago = paidAt?.let { PagoEntity(id = "$id-pago", pedidoId = id, amount = paid, paidAt = it) }
        return SeededPedido(pedido, detalles, pago)
    }

    private fun line(pedido: DemoPedido, index: Int, item: Pair<String, Int>): DetallePedidoEntity {
        val price = DemoCatalog.price(item.first)
        return DetallePedidoEntity(
            id = "${pedido.id}-line-$index", pedidoId = pedido.id, productoId = item.first,
            productName = DemoCatalog.productoName(item.first), quantity = item.second,
            unitPrice = price, catalogPrice = price, notes = null,
        )
    }

    private fun statusFor(paidShare: Double): String = when {
        paidShare <= 0.0 -> "PENDING"
        paidShare >= 1.0 -> "PAID"
        else -> "PARTIAL"
    }
}
