package com.restrusher.ecomercecarlosv.screenshots.data

import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
import com.restrusher.ecomercecarlosv.screenshots.data.DemoIds as Id
import javax.inject.Inject

/**
 * Writes the demo data into the in-memory database. Parents go in before children because the
 * foreign keys are enforced: mercado → cliente → pedido → detalle / pago.
 */
class DemoDataSeeder @Inject constructor(private val db: AppDatabase) {

    suspend fun seed() {
        db.umbralesDao().insert(DemoCatalog.umbrales)
        DemoCatalog.users.forEach { db.userDao().insert(it) }
        DemoCatalog.mercados.forEach { db.mercadoDao().insert(it) }
        DemoClientes.all.forEach { db.clienteDao().insert(it) }
        DemoCatalog.productos.forEach { db.productoDao().insert(it) }
        DemoPedidos.seeded().forEach { seedPedido(it) }
        db.pedidoDao().insert(DemoPedidos.rosaSaldoExtra)
    }

    /** A few writes still waiting to upload, so the Sincronización screen has rows to list. */
    suspend fun seedPendingSync() {
        val dao = db.syncOperationDao()
        dao.enqueue(op(EntityType.PEDIDO, "pedido-rosa-3", "Bs. 146,00"))
        dao.enqueue(op(EntityType.CLIENTE, "cliente-luis", "Luis Condori"))
        dao.enqueue(op(EntityType.PRODUCTO, Id.PRODUCTO_CAFE, DemoCatalog.productoName(Id.PRODUCTO_CAFE)))
    }

    private suspend fun seedPedido(seeded: SeededPedido) {
        db.pedidoDao().insert(seeded.pedido)
        db.detallePedidoDao().insertAll(seeded.lines)
        seeded.pago?.let { db.pagoDao().insert(it) }
    }

    private fun op(entityType: String, entityId: String, label: String) = SyncOperationEntity(
        entityType = entityType, entityId = entityId, operation = SyncOp.UPSERT,
        createdAt = DemoClock.now, entityLabel = label,
    )
}
