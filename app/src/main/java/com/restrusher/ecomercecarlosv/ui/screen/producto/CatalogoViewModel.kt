package com.restrusher.ecomercecarlosv.ui.screen.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.ProductoRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshFailed = MutableStateFlow(false)

    val uiState = combine(
        productoRepository.getAll(),
        _searchQuery,
        sessionManager.currentUser,
        productoRepository.isSyncing,
    ) { productos, query, user, isSyncing ->
        val filtered = if (query.isBlank()) productos
        else productos.filter { p ->
            p.name.contains(query, ignoreCase = true) ||
                p.description?.contains(query, ignoreCase = true) == true
        }
        CatalogoUiState(productos = filtered, searchQuery = query, isLoading = isSyncing && productos.isEmpty(), canWrite = user?.role != UserRole.INVITADO)
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.combine(_refreshFailed) { state, failed ->
        state.copy(refreshFailed = failed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogoUiState(isLoading = true),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onRefresh() {
        _refreshFailed.value = false
        viewModelScope.launch {
            _isRefreshing.value = true
            val success = productoRepository.refresh()
            _isRefreshing.value = false
            _refreshFailed.value = !success
        }
    }

    fun onRefreshErrorDismissed() { _refreshFailed.value = false }
}
