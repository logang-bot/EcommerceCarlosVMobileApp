package com.restrusher.ecomercecarlosv.ui.screen.busqueda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BusquedaViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState = combine(
        _query,
        clienteRepository.getAllIncludingBlacklisted(),
        mercadoRepository.getAll(),
        clienteRepository.isSyncing,
    ) { query, clientes, mercados, isSyncing ->
        val isLoading = isSyncing && clientes.isEmpty()
        if (query.isBlank()) return@combine BusquedaUiState(query = query, isLoading = isLoading)

        val q = query.trim().lowercase()
        val mercadoIndex = mercados.associateBy { it.id }
        val clientesPerMercado = clientes.groupBy { it.mercadoId }

        val clienteResults = clientes
            .filter { c ->
                !c.isBlacklisted && (
                    c.name.lowercase().contains(q) ||
                        c.phones.any { it.contains(q) }
                )
            }
            .map { c ->
                ClienteSearchResult(
                    clienteId = c.id,
                    name = c.name,
                    photoUrl = c.photoUrl,
                    mercadoName = mercadoIndex[c.mercadoId]?.name.orEmpty(),
                    status = ClientStatus.AL_DIA,
                    balance = 0.0,
                )
            }

        val blacklistResults = clientes
            .filter { c ->
                c.isBlacklisted && (
                    c.name.lowercase().contains(q) ||
                        c.phones.any { it.contains(q) }
                )
            }
            .map { c ->
                BlacklistSearchResult(
                    clienteId = c.id,
                    name = c.name,
                    photoUrl = c.photoUrl,
                    mercadoName = mercadoIndex[c.mercadoId]?.name.orEmpty(),
                    balance = c.blacklistBalance,
                )
            }

        val mercadoResults = mercados
            .filter { m -> m.name.lowercase().contains(q) }
            .map { m ->
                MercadoSearchResult(
                    mercadoId = m.id,
                    name = m.name,
                    photoUrl = m.photoUrl,
                    clientesCount = clientesPerMercado[m.id]?.size ?: 0,
                )
            }

        BusquedaUiState(
            query = query,
            clienteResults = clienteResults,
            blacklistResults = blacklistResults,
            mercadoResults = mercadoResults,
            isLoading = isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BusquedaUiState(),
    )

    fun onQueryChange(value: String) { _query.value = value }
    fun clearQuery() { _query.value = "" }
}
