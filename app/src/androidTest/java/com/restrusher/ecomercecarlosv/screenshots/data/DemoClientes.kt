package com.restrusher.ecomercecarlosv.screenshots.data

import com.restrusher.ecomercecarlosv.data.local.entity.ClienteEntity
import com.restrusher.ecomercecarlosv.screenshots.data.DemoClock.daysAgo
import com.restrusher.ecomercecarlosv.screenshots.data.DemoIds as Id

/** A cliente as the demo describes it; [phone] is the only number stored. */
private data class DemoCliente(
    val id: String,
    val mercadoId: String,
    val name: String,
    val description: String,
    val phone: String,
)

/** Why and for how much a blacklisted cliente was flagged. */
private data class Blacklisting(val reason: String, val balance: Double, val daysAgo: Int)

/**
 * Clientes spread over every mercado. Their pedidos in [DemoPedidos] are shaped so the Clientes
 * list shows all three traffic-light states, and two clientes fill the Lista Negra screen.
 */
object DemoClientes {

    const val ROSA_NAME = "Rosa Quispe"

    val active: List<ClienteEntity> = listOf(
        DemoCliente(Id.CLIENTE_ROSA, Id.MERCADO_RODRIGUEZ, ROSA_NAME, "Puesto 14 · Abarrotes", "71520034"),
        DemoCliente("cliente-juan", Id.MERCADO_RODRIGUEZ, "Juan Mamani", "Puesto 3 · Verduras", "76201145"),
        DemoCliente("cliente-carmen", Id.MERCADO_RODRIGUEZ, "Carmen Choque", "Puesto 22 · Frutas", "70114562"),
        DemoCliente("cliente-luis", Id.MERCADO_RODRIGUEZ, "Luis Condori", "Puesto 8 · Panadería", "79933410"),
        DemoCliente("cliente-teresa", Id.MERCADO_RODRIGUEZ, "Teresa Flores", "Puesto 31 · Comedor", "72288190"),
        DemoCliente("cliente-ana", Id.MERCADO_LANZA, "Ana Gutiérrez", "Puesto 5 · Jugos", "71004521"),
        DemoCliente("cliente-miguel", Id.MERCADO_LANZA, "Miguel Ticona", "Puesto 40 · Abarrotes", "76655012"),
        DemoCliente("cliente-sofia", Id.MERCADO_LANZA, "Sofía Apaza", "Puesto 12 · Lácteos", "70398877"),
        DemoCliente("cliente-jorge", Id.MERCADO_CAMACHO, "Jorge Limachi", "Puesto 9 · Cafetería", "73344120"),
        DemoCliente("cliente-elena", Id.MERCADO_CAMACHO, "Elena Huanca", "Puesto 17 · Pollería", "77812003"),
        DemoCliente("cliente-raul", Id.MERCADO_SOPOCACHI, "Raúl Calle", "Puesto 2 · Repostería", "71166543"),
    ).map { it.toEntity(blacklisting = null) }

    val blacklisted: List<ClienteEntity> = listOf(
        DemoCliente("cliente-pedro", Id.MERCADO_RODRIGUEZ, "Pedro Alanoca", "Puesto 27 · Carnes", "79001234")
            .toEntity(Blacklisting("Dejó de pagar y cambió de puesto", 112.50, daysAgo = 20)),
        DemoCliente("cliente-wilma", Id.MERCADO_LANZA, "Wilma Torrez", "Puesto 33 · Abarrotes", "76543210")
            .toEntity(Blacklisting("Pagos rechazados en tres ocasiones", 340.00, daysAgo = 12)),
    )

    val all: List<ClienteEntity> get() = active + blacklisted

    private fun DemoCliente.toEntity(blacklisting: Blacklisting?) = ClienteEntity(
        id = id, mercadoId = mercadoId, name = name, description = description,
        photoUrl = null, phones = phone, mapsUrl = null,
        isBlacklisted = blacklisting != null,
        blacklistReason = blacklisting?.reason,
        blacklistedAt = blacklisting?.let { daysAgo(it.daysAgo) },
        blacklistBalance = blacklisting?.balance ?: 0.0,
        createdAt = daysAgo(250),
    )
}
