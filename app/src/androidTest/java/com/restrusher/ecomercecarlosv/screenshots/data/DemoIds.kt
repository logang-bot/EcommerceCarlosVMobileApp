package com.restrusher.ecomercecarlosv.screenshots.data

/** Ids the screenshot flows navigate by. Names are looked up through [DemoCatalog]. */
object DemoIds {
    const val OWNER = "user-carlos"

    const val MERCADO_RODRIGUEZ = "mercado-rodriguez"
    const val MERCADO_LANZA = "mercado-lanza"
    const val MERCADO_CAMACHO = "mercado-camacho"
    const val MERCADO_SOPOCACHI = "mercado-sopocachi"

    const val CLIENTE_ROSA = "cliente-rosa"
    const val PEDIDO_ROSA_PARCIAL = "pedido-rosa-1"

    const val PRODUCTO_ARROZ = "producto-arroz"
    const val PRODUCTO_AZUCAR = "producto-azucar"
    const val PRODUCTO_ACEITE = "producto-aceite"
    const val PRODUCTO_HARINA = "producto-harina"
    const val PRODUCTO_FIDEO = "producto-fideo"
    const val PRODUCTO_LECHE = "producto-leche"
    const val PRODUCTO_CAFE = "producto-cafe"
    const val PRODUCTO_HUEVOS = "producto-huevos"
}

/**
 * One instant for the whole run, so a date the seeder writes and the date a test formats to find
 * it on screen always agree.
 */
object DemoClock {
    private const val DAY_MS = 24L * 60 * 60 * 1000

    val now: Long = System.currentTimeMillis()

    fun daysAgo(days: Int): Long = now - days * DAY_MS
}
