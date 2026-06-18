package com.restrusher.ecomercecarlosv.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_operations",
    indices = [Index("entityType"), Index("entityId"), Index("createdAt")],
)
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val entityLabel: String = "",
)

object EntityType {
    const val MERCADO = "MERCADO"
    const val CLIENTE = "CLIENTE"
    const val PRODUCTO = "PRODUCTO"
    const val PEDIDO = "PEDIDO"
}

object SyncOp {
    const val UPSERT = "UPSERT"
    const val DELETE = "DELETE"
}
