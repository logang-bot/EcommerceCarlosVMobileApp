package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.ClientesRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val mercadoId: String = savedStateHandle.toRoute<ClientesRoute>().mercadoId

    private val _sortMode = MutableStateFlow(ClienteSortMode.AZ)
    private val _mercadoName = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")

    val uiState = combine(
        clienteRepository.getByMercado(mercadoId),
        _sortMode,
        _mercadoName,
        _searchQuery,
    ) { clientes, sort, name, query ->
        val filtered = if (query.isBlank()) clientes
        else clientes.filter { it.name.contains(query, ignoreCase = true) }
        // Balance and status are computed from pedidos (Phase 4). Defaults: AL_DIA / 0.0.
        val models = filtered.map { ClienteUiModel(it, ClientStatus.AL_DIA, 0.0) }
        val sorted = when (sort) {
            ClienteSortMode.AZ -> models.sortedBy { it.cliente.name }
            ClienteSortMode.CRITICOS_FIRST -> models.sortedWith(
                compareBy<ClienteUiModel> { it.status.ordinal }.thenByDescending { it.balance },
            )
            ClienteSortMode.MAYOR_SALDO -> models.sortedByDescending { it.balance }
            ClienteSortMode.SOLO_CON_DEUDA -> models.filter { it.balance > 0 }.sortedByDescending { it.balance }
        }
        ClientesUiState(
            clientes = sorted,
            mercadoId = mercadoId,
            mercadoName = name,
            sortMode = sort,
            searchQuery = query,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientesUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            _mercadoName.value = mercadoRepository.getById(mercadoId)?.name.orEmpty()
        }
    }

    fun onSortChange(mode: ClienteSortMode) { _sortMode.value = mode }
    fun onSearchChange(query: String) { _searchQuery.value = query }
}
