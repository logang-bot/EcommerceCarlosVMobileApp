package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.presentation.screens.AgregarListaNegraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreacionPedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateClienteRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetallePedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.SaldoExtraRoute
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme

@Composable
fun DetalleClienteScreen(
    navController: NavController,
    viewModel: DetalleClienteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleClienteContent(
        state = state,
        onBack = { navController.popBackStack() },
        onEditClick = { clienteId, mercadoId ->
            navController.navigate(CreateClienteRoute(mercadoId = mercadoId, clienteId = clienteId))
        },
        onListaNegraClick = { state.cliente?.let { navController.navigate(AgregarListaNegraRoute(it.id)) } },
        onQuitarListaNegraClick = { viewModel.onQuitarListaNegraClick() },
        onDismissUnblacklistSheet = { viewModel.dismissUnblacklistSheet() },
        onUnblacklistRestore = { viewModel.unblacklistRestore() },
        onUnblacklistMarkAllPaid = { viewModel.unblacklistMarkAllPaid() },
        onSaldoExtraClick = { state.cliente?.let { navController.navigate(SaldoExtraRoute(it.id)) } },
        onNuevoPedidoClick = {
            state.cliente?.let { c ->
                navController.navigate(
                    CreacionPedidoRoute(
                        clienteId = c.id,
                        clienteName = c.name,
                        mercadoName = "",
                    ),
                )
            }
        },
        onPedidoClick = { pedidoId -> navController.navigate(DetallePedidoRoute(pedidoId)) },
    )
}

@Composable
private fun DetalleClienteContent(
    state: DetalleClienteUiState,
    onBack: () -> Unit,
    onEditClick: (clienteId: String, mercadoId: String) -> Unit,
    onListaNegraClick: () -> Unit,
    onQuitarListaNegraClick: () -> Unit,
    onDismissUnblacklistSheet: () -> Unit = {},
    onUnblacklistRestore: () -> Unit = {},
    onUnblacklistMarkAllPaid: () -> Unit = {},
    onSaldoExtraClick: () -> Unit,
    onNuevoPedidoClick: () -> Unit = {},
    onPedidoClick: (pedidoId: String) -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.detalle_cliente_title),
                onBack = onBack,
                actions = {
                    if (state.cliente != null) {
                        IconButton(onClick = { onEditClick(state.cliente.id, state.cliente.mercadoId) }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.cliente != null && !state.cliente.isBlacklisted) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.pedidos_nuevo)) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    onClick = onNuevoPedidoClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            state.cliente != null -> ClienteData(
                cliente = state.cliente,
                status = state.status,
                balance = state.balance,
                pedidos = state.pedidos,
                innerPadding = innerPadding,
                onListaNegraClick = onListaNegraClick,
                onQuitarListaNegraClick = onQuitarListaNegraClick,
                onSaldoExtraClick = onSaldoExtraClick,
                onPedidoClick = onPedidoClick,
                onNuevoPedidoClick = onNuevoPedidoClick,
            )
        }

        if (state.showUnblacklistSheet) {
            QuitarListaNegraSheet(
                onDismiss = onDismissUnblacklistSheet,
                onRestore = onUnblacklistRestore,
                onMarkAllPaid = onUnblacklistMarkAllPaid,
            )
        }
    }
}

@Composable
private fun ClienteData(
    cliente: Cliente,
    status: ClientStatus,
    balance: Double,
    pedidos: List<Pedido>,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onListaNegraClick: () -> Unit,
    onQuitarListaNegraClick: () -> Unit,
    onSaldoExtraClick: () -> Unit,
    onPedidoClick: (String) -> Unit,
    onNuevoPedidoClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (cliente.isBlacklisted) {
            BlacklistBanner(blacklistedAt = cliente.blacklistedAt)
        }
        ClienteHeader(cliente = cliente)
        if (cliente.isBlacklisted && cliente.blacklistIsManualAmount) {
            BalanceBlock(status = status, balance = balance, modifier = Modifier.alpha(0.38f))
            Spacer(Modifier.height(10.dp))
            BlacklistBalanceBlock(balance = cliente.blacklistBalance)
        } else {
            BalanceBlock(status = status, balance = balance)
        }
        Spacer(Modifier.height(12.dp))
        ActionButtons(
            isBlacklisted = cliente.isBlacklisted,
            onListaNegraClick = onListaNegraClick,
            onQuitarListaNegraClick = onQuitarListaNegraClick,
            onSaldoExtraClick = onSaldoExtraClick,
        )
        Spacer(Modifier.height(24.dp))
        PedidosSection(
            pedidos = pedidos,
            onPedidoClick = onPedidoClick,
            onNuevoPedidoClick = if (!cliente.isBlacklisted) onNuevoPedidoClick else null,
        )
        Spacer(Modifier.height(80.dp))
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewPedidos = listOf(
    Pedido(id = "o1", clienteId = "c1", status = PedidoStatus.PENDING, total = 47.50, paid = 0.0, createdAt = 1748822400000L, itemCount = 3),
    Pedido(id = "o2", clienteId = "c1", status = PedidoStatus.PARTIAL, total = 112.00, paid = 50.0, createdAt = 1748390400000L, itemCount = 5),
    Pedido(id = "o3", clienteId = "c1", status = PedidoStatus.PAID, total = 24.00, paid = 24.0, createdAt = 1747872000000L, itemCount = 2),
    Pedido(id = "o4", clienteId = "c1", status = PedidoStatus.PENDING, total = 60.00, paid = 0.0, createdAt = 1747353600000L, isSaldoExtra = true, notes = "Envases retornables"),
)

@Preview(name = "DetalleCliente — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetalleClienteScreenPreview() {
    val cliente = Cliente(
        id = "c1", mercadoId = "m1", name = "Ana Rodríguez",
        description = "Puesto 14 · verduras", phones = listOf("0414-2230198"),
        mapsUrl = "https://maps.google.com/?q=Mercado+Central", createdAt = 0L,
    )
    EcomerceCarlosVTheme(darkTheme = true) {
        DetalleClienteContent(
            state = DetalleClienteUiState(cliente = cliente, status = ClientStatus.CRITICO, balance = 340.0, pedidos = previewPedidos, isLoading = false),
            onBack = {}, onEditClick = { _, _ -> }, onListaNegraClick = {}, onQuitarListaNegraClick = {}, onSaldoExtraClick = {},
        )
    }
}

@Preview(name = "DetalleCliente — blacklisted dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetalleClienteListaNegraPreview() {
    val cliente = Cliente(
        id = "c1", mercadoId = "m1", name = "Ana Rodríguez",
        description = "Puesto 14 · verduras", phones = listOf("0414-2230198"),
        mapsUrl = null, createdAt = 0L,
        isBlacklisted = true, blacklistedAt = 1747526400000L,
    )
    EcomerceCarlosVTheme(darkTheme = true) {
        DetalleClienteContent(
            state = DetalleClienteUiState(cliente = cliente, status = ClientStatus.CRITICO, balance = 340.0, isLoading = false),
            onBack = {}, onEditClick = { _, _ -> }, onListaNegraClick = {}, onQuitarListaNegraClick = {}, onSaldoExtraClick = {},
        )
    }
}
