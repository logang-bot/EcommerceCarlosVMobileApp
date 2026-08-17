package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class ListaNegraViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(ListaNegraUiState())
    val uiState: StateFlow<ListaNegraUiState> = _uiState.asStateFlow()

    init {
        combine(
            clienteRepository.getBlacklisted(),
            mercadoRepository.getAll(),
            _query,
            clienteRepository.isSyncing,
        ) { clientes, mercados, query, isSyncing ->
            val mercadoMap = mercados.associateBy { it.id }
            ListaNegraUiState(
                items = clientes.map { c ->
                    BlacklistUiModel(
                        clienteId = c.id,
                        name = c.name,
                        photoUrl = c.photoUrl,
                        mercadoName = mercadoMap[c.mercadoId]?.name ?: "",
                        blacklistBalance = c.blacklistBalance,
                        blacklistReason = c.blacklistReason,
                        blacklistedAt = c.blacklistedAt,
                    )
                },
                query = query,
                isLoading = isSyncing && clientes.isEmpty(),
            )
        }.combine(_isRefreshing) { state, refreshing ->
            state.copy(isRefreshing = refreshing)
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            clienteRepository.refresh()
            _isRefreshing.value = false
        }
    }

}
