package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tag
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.presentation.screens.AgregarListaNegraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreacionPedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateClienteRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetallePedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.SaldoExtraRoute
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        onQuitarListaNegraClick = { viewModel.unblacklist() },
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
                Modifier.padding(innerPadding).fillMaxSize(),
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
        BalanceBlock(status = status, balance = balance)
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

@Composable
private fun BlacklistBanner(blacklistedAt: Long?) {
    val ext = MaterialTheme.extendedColors
    val dateText = blacklistedAt?.let {
        SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(it))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(ext.redTint)
            .border(1.dp, Color(0x47F05A50), RoundedCornerShape(13.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(
            Icons.Default.Block,
            contentDescription = null,
            tint = ext.redText,
            modifier = Modifier.size(20.dp).padding(top = 1.dp),
        )
        Column {
            Text(
                text = stringResource(R.string.detalle_cliente_lista_negra_title),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = ext.redText,
            )
            Text(
                text = if (dateText != null) {
                    stringResource(R.string.detalle_cliente_lista_negra_subtitle, dateText)
                } else {
                    stringResource(R.string.detalle_cliente_lista_negra_subtitle, "—")
                },
                fontSize = 12.5.sp,
                color = MaterialTheme.extendedColors.text2,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ClienteHeader(cliente: Cliente) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            ClienteAvatar(name = cliente.name, size = 76.dp)
            if (cliente.isBlacklisted) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF05A50))
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(cliente.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
        if (cliente.description.isNotBlank()) {
            Text(
                text = cliente.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.extendedColors.text2,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.padding(top = 14.dp),
        ) {
            cliente.phones.firstOrNull()?.let { phone ->
                ContactChip(
                    icon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                    label = phone,
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                    },
                )
            }
            if (!cliente.mapsUrl.isNullOrBlank()) {
                ContactChip(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                    label = stringResource(R.string.detalle_cliente_ubicacion),
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cliente.mapsUrl))) }
                    },
                )
            }
        }
    }
}

@Composable
private fun ContactChip(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border2, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        icon()
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, fontSize = 13.5.sp)
    }
}

@Composable
private fun BalanceBlock(status: ClientStatus, balance: Double) {
    val ext = MaterialTheme.extendedColors
    val (gradStart, gradEnd, borderColor) = when (status) {
        ClientStatus.CRITICO -> Triple(Color(0x26F05A50), Color(0x0AF05A50), Color(0x38F05A50))
        ClientStatus.ADVERTENCIA -> Triple(Color(0x26E7B23E), Color(0x0AE7B23E), Color(0x38E7B23E))
        ClientStatus.AL_DIA -> Triple(Color(0x2436C880), Color(0x0836C880), Color(0x3336C880))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors = listOf(gradStart, gradEnd)))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(stringResource(R.string.detalle_cliente_saldo_total), style = MaterialTheme.typography.bodySmall, color = ext.text2, fontWeight = FontWeight.Medium)
            Text(
                text = formatBalance(balance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        ClienteStatusBadge(status = status, size = BadgeSize.MD)
    }
}

@Composable
private fun ActionButtons(
    isBlacklisted: Boolean,
    onListaNegraClick: () -> Unit,
    onQuitarListaNegraClick: () -> Unit,
    onSaldoExtraClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isBlacklisted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth().height(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                    .clickable(onClick = onQuitarListaNegraClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.clientes_quitar_lista_negra), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth().height(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ext.redTint)
                    .border(1.dp, ext.redText.copy(alpha = 0.25f), RoundedCornerShape(13.dp))
                    .clickable(onClick = onListaNegraClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.clientes_agregar_lista_negra), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = ext.redText)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth().height(46.dp)
                .then(if (isBlacklisted) Modifier.alpha(0.5f) else Modifier)
                .clip(RoundedCornerShape(13.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                .then(if (!isBlacklisted) Modifier.clickable(onClick = onSaldoExtraClick) else Modifier)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Tag,
                contentDescription = null,
                tint = ext.amberText,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.clientes_agregar_saldo_extra),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
            )
        }
    }
}

@Composable
private fun PedidosSection(pedidos: List<Pedido>, onPedidoClick: (String) -> Unit, onNuevoPedidoClick: (() -> Unit)? = null) {
    val ext = MaterialTheme.extendedColors
    Text(
        text = stringResource(R.string.detalle_cliente_pedidos_section).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = ext.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
    )
    if (pedidos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Receipt,
            title = stringResource(R.string.detalle_cliente_pedidos_empty_title),
            subtitle = stringResource(R.string.detalle_cliente_pedidos_empty_subtitle),
            hint = stringResource(R.string.detalle_cliente_pedidos_empty_hint),
            compact = true,
            modifier = Modifier.height(240.dp),
            onActionClick = onNuevoPedidoClick,
        )
    } else {
        pedidos.forEach { pedido ->
            PedidoRow(pedido = pedido, onClick = { onPedidoClick(pedido.id) })
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetalleClienteScreenPreview() {
    val cliente = Cliente(
        id = "c1", mercadoId = "m1", name = "Ana Rodríguez",
        description = "Puesto 14 · verduras", phones = listOf("0414-2230198"),
        mapsUrl = "https://maps.google.com/?q=Mercado+Central", createdAt = 0L,
    )
    EcomerceCarlosVTheme(darkTheme = true) {
        DetalleClienteContent(
            state = DetalleClienteUiState(cliente = cliente, status = ClientStatus.CRITICO, balance = 340.0, isLoading = false),
            onBack = {}, onEditClick = { _, _ -> }, onListaNegraClick = {}, onQuitarListaNegraClick = {}, onSaldoExtraClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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
