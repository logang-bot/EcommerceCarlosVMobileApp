package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.Producto
import com.restrusher.ecomercecarlosv.domain.repository.ProductoRepository
import com.restrusher.ecomercecarlosv.domain.usecase.CreatePedidoUseCase
import com.restrusher.ecomercecarlosv.presentation.screens.CreacionPedidoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreacionPedidoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    productoRepository: ProductoRepository,
    private val createPedidoUseCase: CreatePedidoUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CreacionPedidoRoute>()

    private val _uiState = MutableStateFlow(
        CreacionPedidoUiState(
            clienteId = route.clienteId,
            clienteName = route.clienteName,
            mercadoName = route.mercadoName,
        ),
    )
    val uiState: StateFlow<CreacionPedidoUiState> = _uiState.asStateFlow()

    init {
        productoRepository.getAll()
            .onEach { productos -> _uiState.update { it.copy(productos = productos) } }
            .launchIn(viewModelScope)
    }

    fun toggleProduct(producto: Producto) {
        val inCart = _uiState.value.cart.any { it.productoId == producto.id }
        if (inCart) {
            _uiState.update { it.copy(cart = it.cart.filter { item -> item.productoId != producto.id }) }
        } else {
            val newItem = CartItem(
                productoId = producto.id,
                productName = producto.name,
                unitPrice = producto.price,
                catalogPrice = producto.price,
            )
            _uiState.update { it.copy(cart = it.cart + newItem) }
        }
    }

    fun adjustQuantity(productoId: String, delta: Int) {
        _uiState.update { state ->
            val cart = state.cart.mapNotNull { item ->
                if (item.productoId != productoId) item
                else item.copy(quantity = item.quantity + delta).takeIf { it.quantity > 0 }
            }
            state.copy(cart = cart)
        }
    }

    fun removeFromCart(productoId: String) {
        _uiState.update { it.copy(cart = it.cart.filter { item -> item.productoId != productoId }) }
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active, searchQuery = if (!active) "" else it.searchQuery) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onEditItem(productoId: String) {
        _uiState.update { it.copy(editingItem = it.cart.find { item -> item.productoId == productoId }) }
    }

    fun onSaveEditItem(updated: CartItem) {
        _uiState.update { state ->
            state.copy(
                cart = state.cart.map { if (it.productoId == updated.productoId) updated else it },
                editingItem = null,
            )
        }
    }

    fun dismissEditItem() = _uiState.update { it.copy(editingItem = null) }

    fun showPaymentSheet() = _uiState.update { it.copy(showPaymentSheet = true) }

    fun dismissPaymentSheet() = _uiState.update { it.copy(showPaymentSheet = false) }

    fun confirmPedido(initialPayment: Double) {
        val state = _uiState.value
        if (state.cart.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val id = createPedidoUseCase(state.clienteId, state.cart, initialPayment)
            _uiState.update { it.copy(isSaving = false, savedPedidoId = id, showPaymentSheet = false) }
        }
    }

    fun onNavigationHandled() = _uiState.update { it.copy(savedPedidoId = null) }
}
