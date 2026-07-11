package com.restrusher.ecomercecarlosv.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UmbralesDto(
    val id: String = "global",
    @SerialName("monto_maximo") val montoMaximo: Double,
    @SerialName("dias_maximos") val diasMaximos: Int,
    @SerialName("updated_at") val updatedAt: Long = 0L,
)
