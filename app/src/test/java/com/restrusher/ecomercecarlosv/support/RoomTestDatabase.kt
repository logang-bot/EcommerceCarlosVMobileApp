package com.restrusher.ecomercecarlosv.support

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.fixtures.clienteEntity
import com.restrusher.ecomercecarlosv.fixtures.mercadoEntity

/**
 * A real Room database backed by memory. Foreign keys are enforced exactly as on device, which is
 * the point — a pedido cannot be inserted without its cliente, and a cliente without its mercado.
 */
fun createTestDatabase(): AppDatabase =
    Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()

/**
 * Inserts the mercado and cliente rows that the pedido foreign keys require. Call before writing
 * any pedido, otherwise the insert fails on a constraint rather than on the behaviour under test.
 */
suspend fun AppDatabase.seedMercadoAndCliente(
    mercadoId: String = "mercado-1",
    clienteId: String = "cliente-1",
) {
    mercadoDao().insert(mercadoEntity(id = mercadoId))
    clienteDao().insert(clienteEntity(id = clienteId).copy(mercadoId = mercadoId, isBlacklisted = false))
}
