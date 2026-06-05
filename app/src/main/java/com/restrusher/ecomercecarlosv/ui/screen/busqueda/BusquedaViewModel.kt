package com.restrusher.ecomercecarlosv.ui.screen.busqueda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BusquedaViewModel @Inject constructor() : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState = _query.map { q ->
        // TODO Phase 3: replace with real ClienteRepository.search(q)
        BusquedaUiState(query = q, results = emptyList(), isSearching = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BusquedaUiState(),
    )

    fun onQueryChange(value: String) { _query.value = value }
    fun clearQuery() { _query.value = "" }
}
