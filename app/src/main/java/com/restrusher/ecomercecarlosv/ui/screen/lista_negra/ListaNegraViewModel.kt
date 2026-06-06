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

@HiltViewModel
class ListaNegraViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _uiState = MutableStateFlow(ListaNegraUiState())
    val uiState: StateFlow<ListaNegraUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                clienteRepository.getBlacklisted(),
                mercadoRepository.getAll(),
                _query,
            ) { clientes, mercados, query ->
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
                    isLoading = false,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onQueryChange(query: String) { _query.value = query }
}
