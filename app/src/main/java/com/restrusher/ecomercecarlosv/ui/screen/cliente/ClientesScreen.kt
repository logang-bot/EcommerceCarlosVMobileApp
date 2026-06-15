package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.presentation.screens.CreateClienteRoute
import com.restrusher.ecomercecarlosv.presentation.screens.ListaNegraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleClienteRoute
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ClientesScreen(
    navController: NavController,
    viewModel: ClientesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ClientesContent(
        state = state,
        onBack = { navController.popBackStack() },
        onClienteClick = { navController.navigate(DetalleClienteRoute(it)) },
        onCreateClick = { navController.navigate(CreateClienteRoute(viewModel.mercadoId)) },
        onListaNegraClick = { navController.navigate(ListaNegraRoute) },
        onSortChange = viewModel::onSortChange,
        onSearchChange = viewModel::onSearchChange,
    )
}

@Composable
private fun ClientesContent(
    state: ClientesUiState,
    onBack: () -> Unit,
    onClienteClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onListaNegraClick: () -> Unit,
    onSortChange: (ClienteSortMode) -> Unit,
    onSearchChange: (String) -> Unit,
) {
    val count = state.clientes.size
    var searchActive by remember { mutableStateOf(false) }
    val ext = MaterialTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                PedidosTopBar(
                    title = state.mercadoName.ifBlank { stringResource(R.string.clientes_title_fallback) },
                    subtitle = if (count > 0) stringResource(R.string.clientes_subtitle, count) else null,
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { searchActive = !searchActive }) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = if (searchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                        ClientesFilterMenu(active = state.sortMode, onSelect = onSortChange)
                    },
                )
                AnimatedVisibility(visible = searchActive) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.clientes_search_placeholder), color = ext.text3) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ext.text2, modifier = Modifier.size(20.dp)) },
                        trailingIcon = if (state.searchQuery.isNotEmpty()) {
                            { IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Close, contentDescription = null) } }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = clienteFieldColors(),
                    )
                }
            }
        },
        floatingActionButton = {
            if (state.canWrite) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.clientes_fab)) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = onCreateClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                )
            }
        },
    ) { innerPadding ->
        if (state.clientes.isEmpty() && !state.isLoading) {
            EmptyState(
                modifier = Modifier.padding(innerPadding),
                icon = Icons.Default.Person,
                title = stringResource(R.string.clientes_empty_title),
                subtitle = stringResource(R.string.clientes_empty_subtitle),
                hint = if (state.canWrite) stringResource(R.string.clientes_empty_hint) else null,
                onActionClick = if (state.canWrite) onCreateClick else null,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                items(state.clientes, key = { it.cliente.id }) { model ->
                    ClienteRow(
                        model = model,
                        onClick = { onClienteClick(model.cliente.id) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 82.dp),
                        color = MaterialTheme.extendedColors.border,
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    ListaNegraButton(onClick = onListaNegraClick)
                }
            }
        }
    }
}

@Composable
internal fun ClienteRow(
    modifier: Modifier = Modifier,
    model: ClienteUiModel,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val isDark = isSystemInDarkTheme()
    val bgAlpha = if (isDark) 0.30f else 0.22f

    val bgColor = when (model.status) {
        ClientStatus.CRITICO -> ext.redTint.copy(alpha = bgAlpha)
        ClientStatus.ADVERTENCIA -> ext.amberTint.copy(alpha = bgAlpha)
        ClientStatus.AL_DIA -> ext.greenTint.copy(alpha = bgAlpha)
    }
    val accentColor = when (model.status) {
        ClientStatus.CRITICO -> ext.redTint.copy(alpha = 1f)
        ClientStatus.ADVERTENCIA -> ext.amber
        ClientStatus.AL_DIA -> ext.green
    }
    val balanceColor = when {
        model.balance == 0.0 -> ext.text3
        model.status == ClientStatus.CRITICO -> ext.redText
        model.status == ClientStatus.ADVERTENCIA -> ext.amberText
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .drawBehind {
                drawRect(color = bgColor)
                drawRect(color = accentColor, size = Size(6.dp.toPx(), size.height))
            }
            .padding(start = 22.dp, end = 18.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ClienteAvatar(name = model.cliente.name, photoUrl = model.cliente.photoUrl, size = 46.dp, status = model.status)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.cliente.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.2).sp,
            )
            Text(
                text = model.cliente.description,
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            ClienteStatusBadge(status = model.status)
            Text(
                text = formatBalance(model.balance),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = balanceColor,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ListaNegraButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(9.dp))
        Text(
            text = stringResource(R.string.clientes_ver_lista_negra),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ext.text2,
        )
    }
}

internal fun formatBalance(amount: Double): String {
    val formatted = String.format("%.2f", amount).replace('.', ',')
    return "Bs. $formatted"
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ClientesScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ClientesContent(
            state = ClientesUiState(mercadoName = "Mercado Central"),
            onBack = {}, onClienteClick = {}, onCreateClick = {}, onListaNegraClick = {}, onSortChange = {}, onSearchChange = {},
        )
    }
}
