package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PedidoDto(
    val id: String,
    @SerialName("cliente_id") val clienteId: String,
    val status: String,
    val total: Double,
    val paid: Double,
    val notes: String?,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("paid_at") val paidAt: Long?,
    @SerialName("is_saldo_extra") val isSaldoExtra: Boolean = false,
    @SerialName("updated_at") val updatedAt: Long = 0L,
)
