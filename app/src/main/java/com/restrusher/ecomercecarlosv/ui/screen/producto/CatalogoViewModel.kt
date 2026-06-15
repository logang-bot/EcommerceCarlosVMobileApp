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
import javax.inject.Inject

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    productoRepository: ProductoRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState = combine(
        productoRepository.getAll(),
        _searchQuery,
        sessionManager.currentUser,
    ) { productos, query, user ->
        val filtered = if (query.isBlank()) productos
        else productos.filter { p ->
            p.name.contains(query, ignoreCase = true) ||
                p.description?.contains(query, ignoreCase = true) == true
        }
        CatalogoUiState(productos = filtered, searchQuery = query, isLoading = false, canWrite = user?.role != UserRole.INVITADO)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogoUiState(isLoading = true),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
