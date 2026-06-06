package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun CreacionPedidoScreen(
    navController: NavController,
    viewModel: CreacionPedidoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedPedidoId) {
        if (state.savedPedidoId != null) {
            navController.popBackStack()
            viewModel.onNavigationHandled()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                CreacionTopBar(
                    clienteName = state.clienteName,
                    mercadoName = state.mercadoName,
                    onBack = { navController.popBackStack() },
                )
                if (state.isSearchActive) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClose = { viewModel.setSearchActive(false) },
                    )
                }
            }
        },
        bottomBar = {
            CartPanel(
                cart = state.cart,
                onRemoveFromCart = viewModel::removeFromCart,
                onEditItem = viewModel::onEditItem,
                onConfirm = viewModel::showPaymentSheet,
            )
        },
    ) { padding ->
        if (state.productos.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    icon = Icons.Default.Receipt,
                    title = stringResource(R.string.catalogo_empty_title),
                    subtitle = stringResource(R.string.pedidos_catalogo_vacio_subtitle),
                )
            }
        } else {
            ProductGridSection(
                modifier = Modifier.padding(padding).fillMaxSize(),
                productos = state.filteredProductos,
                cartQuantities = state::cartQuantityFor,
                onToggleProduct = viewModel::toggleProduct,
                onAdjustQuantity = viewModel::adjustQuantity,
                onSearchClick = { viewModel.setSearchActive(true) },
            )
        }
    }

    state.editingItem?.let { item ->
        LineEditSheet(item = item, onSave = viewModel::onSaveEditItem, onDismiss = viewModel::dismissEditItem)
    }

    if (state.showPaymentSheet) {
        PagoSheet(
            total = state.cartTotal,
            clienteName = state.clienteName,
            itemCount = state.cart.size,
            isSaving = state.isSaving,
            onSubmit = viewModel::confirmPedido,
            onDismiss = viewModel::dismissPaymentSheet,
        )
    }
}

@Composable
private fun CreacionTopBar(clienteName: String, mercadoName: String, onBack: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(stringResource(R.string.pedidos_nuevo), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            if (clienteName.isNotBlank()) {
                Text("$clienteName · $mercadoName", fontSize = 12.sp, color = ext.text2, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(ext.surface2)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = ext.text3, modifier = Modifier.size(16.dp))
            }
        } else {
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = ext.text3, modifier = Modifier.size(16.dp))
            }
        }
    }
}
