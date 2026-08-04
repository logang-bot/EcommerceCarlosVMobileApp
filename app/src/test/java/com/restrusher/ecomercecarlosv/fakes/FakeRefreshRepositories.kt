package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Only refresh() carries logic worth faking for the Refresh*DataUseCase suites; the rest are stubs.

class FakeClienteRepository : ClienteRepository {

    data class Blacklisting(
        val id: String,
        val reason: String,
        val balance: Double,
        val at: Long,
        val isManualAmount: Boolean,
    )

    /** Every [blacklist] call, in order, and every [unblacklist] id. */
    val blacklisted = mutableListOf<Blacklisting>()
    val unblacklisted = mutableListOf<String>()

    var refreshResult = true
    var refreshCount = 0
        private set

    private val clientes = MutableStateFlow<List<Cliente>>(emptyList())

    fun givenClientes(vararg items: Cliente) {
        clientes.value = items.toList()
    }

    override val isSyncing: Flow<Boolean> = flowOf(false)

    override fun getAll(): Flow<List<Cliente>> = clientes.map { all -> all.filterNot { it.isBlacklisted } }

    override fun getAllIncludingBlacklisted(): Flow<List<Cliente>> = clientes

    override fun getByMercado(mercadoId: String): Flow<List<Cliente>> =
        clientes.map { all -> all.filter { it.mercadoId == mercadoId } }

    override fun getBlacklisted(): Flow<List<Cliente>> = clientes.map { all -> all.filter { it.isBlacklisted } }

    override fun getByIdFlow(id: String): Flow<Cliente?> = clientes.map { all -> all.find { it.id == id } }

    override suspend fun getById(id: String): Cliente? = clientes.value.find { it.id == id }

    override suspend fun save(cliente: Cliente) {
        clientes.value = clientes.value.filterNot { it.id == cliente.id } + cliente
    }

    override suspend fun delete(id: String) {
        clientes.value = clientes.value.filterNot { it.id == id }
    }

    override suspend fun blacklist(
        id: String,
        reason: String,
        balance: Double,
        at: Long,
        isManualAmount: Boolean,
    ) {
        blacklisted += Blacklisting(id, reason, balance, at, isManualAmount)
    }

    override suspend fun unblacklist(id: String) {
        unblacklisted += id
    }

    override suspend fun refresh(): Boolean {
        refreshCount++
        return refreshResult
    }
}

class FakeMercadoRepository : MercadoRepository {

    var refreshResult = true
    var refreshCount = 0
        private set

    private val mercados = MutableStateFlow<List<Mercado>>(emptyList())

    fun givenMercados(vararg items: Mercado) {
        mercados.value = items.toList()
    }

    override val isSyncing: Flow<Boolean> = flowOf(false)

    override fun getAll(): Flow<List<Mercado>> = mercados

    override fun getByIdFlow(id: String): Flow<Mercado?> = mercados.map { all -> all.find { it.id == id } }

    override suspend fun getById(id: String): Mercado? = mercados.value.find { it.id == id }

    override suspend fun save(mercado: Mercado) {
        mercados.value = mercados.value.filterNot { it.id == mercado.id } + mercado
    }

    override suspend fun delete(id: String) {
        mercados.value = mercados.value.filterNot { it.id == id }
    }

    override suspend fun refresh(): Boolean {
        refreshCount++
        return refreshResult
    }
}
