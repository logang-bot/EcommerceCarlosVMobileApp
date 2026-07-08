package com.restrusher.ecomercecarlosv.ui.screen.busqueda

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleClienteRoute
import com.restrusher.ecomercecarlosv.presentation.screens.ClientesRoute
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay
import com.restrusher.ecomercecarlosv.ui.common.PhotoThumbnail
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun BusquedaScreen(
    navController: NavController,
    viewModel: BusquedaViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BusquedaContent(
        state = state,
        focusRequester = focusRequester,
        onBack = { navController.popBackStack() },
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onClienteClick = { navController.navigate(DetalleClienteRoute(it)) },
        onBlacklistClienteClick = { navController.navigate(DetalleClienteRoute(it)) },
        onMercadoClick = { navController.navigate(ClientesRoute(it)) },
    )
}

@Composable
private fun BusquedaContent(
    state: BusquedaUiState,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onClienteClick: (String) -> Unit,
    onBlacklistClienteClick: (String) -> Unit = {},
    onMercadoClick: (String) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LoadingOverlay(isLoading = state.isLoading) {
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                SearchField(
                    query = state.query,
                    focusRequester = focusRequester,
                    onQueryChange = onQueryChange,
                    onClearQuery = onClearQuery,
                    modifier = Modifier.weight(1f),
                )
            }

            when {
                state.query.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.busqueda_no_query_title),
                        subtitle = stringResource(R.string.busqueda_no_query_subtitle),
                    )
                }
                !state.hasResults -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.busqueda_empty_title),
                        subtitle = stringResource(R.string.busqueda_empty_subtitle),
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (state.clienteResults.isNotEmpty()) {
                            item {
                                SearchGroupLabel(
                                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = ext.text3, modifier = Modifier.size(15.dp)) },
                                    label = stringResource(R.string.busqueda_section_clientes),
                                    count = state.clienteResults.size,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            items(state.clienteResults, key = { it.clienteId }) { result ->
                                ClienteResultRow(result = result, onClick = { onClienteClick(result.clienteId) })
                                HorizontalDivider(modifier = Modifier.padding(start = 75.dp), color = ext.border)
                            }
                        }
                        if (state.blacklistResults.isNotEmpty()) {
                            item {
                                SearchGroupLabel(
                                    icon = { Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(15.dp)) },
                                    label = stringResource(R.string.lista_negra_title),
                                    count = state.blacklistResults.size,
                                    labelColor = ext.redText,
                                    modifier = Modifier.padding(top = 22.dp),
                                )
                            }
                            items(state.blacklistResults, key = { "bl_${it.clienteId}" }) { result ->
                                BlacklistResultRow(result = result, onClick = { onBlacklistClienteClick(result.clienteId) })
                                HorizontalDivider(modifier = Modifier.padding(start = 75.dp), color = ext.border)
                            }
                        }
                        if (state.mercadoResults.isNotEmpty()) {
                            item {
                                SearchGroupLabel(
                                    icon = { Icon(Icons.Default.GridView, contentDescription = null, tint = ext.text3, modifier = Modifier.size(15.dp)) },
                                    label = stringResource(R.string.busqueda_section_mercados),
                                    count = state.mercadoResults.size,
                                    modifier = Modifier.padding(top = 22.dp),
                                )
                            }
                            items(state.mercadoResults, key = { it.mercadoId }) { result ->
                                MercadoResultRow(result = result, onClick = { onMercadoClick(result.mercadoId) })
                                HorizontalDivider(modifier = Modifier.padding(start = 75.dp), color = ext.border)
                            }
                        }
                    }
                }
            }
        }
        } // LoadingOverlay
    }
}

@Composable
private fun SearchGroupLabel(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    count: Int,
    labelColor: Color? = null,
) {
    val ext = MaterialTheme.extendedColors
    val resolvedColor = labelColor ?: ext.text3
    Row(
        modifier = modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = resolvedColor,
            letterSpacing = 0.5.sp,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ext.text4,
        )
    }
}

@Composable
private fun ClienteResultRow(result: ClienteSearchResult, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    val statusColor = when (result.status) {
        ClientStatus.CRITICO -> ext.redText
        ClientStatus.ADVERTENCIA -> ext.amberText
        ClientStatus.AL_DIA -> ext.greenText
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ClienteAvatar(name = result.name, photoUrl = result.photoUrl, size = 42.dp, status = result.status)
        Column(modifier = Modifier.weight(1f)) {
            Text(result.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(result.mercadoName, style = MaterialTheme.typography.bodySmall, color = ext.text2, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = when (result.status) {
                        ClientStatus.CRITICO -> stringResource(R.string.status_critico)
                        ClientStatus.ADVERTENCIA -> stringResource(R.string.status_advertencia)
                        ClientStatus.AL_DIA -> stringResource(R.string.status_al_dia)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (result.balance > 0.0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Bs. %.2f".format(result.balance),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun BlacklistResultRow(result: BlacklistSearchResult, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(modifier = Modifier.size(42.dp)) {
            ClienteAvatar(name = result.name, photoUrl = result.photoUrl, size = 42.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(ext.redText)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
            ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(result.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(result.mercadoName, style = MaterialTheme.typography.bodySmall, color = ext.text2, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ext.redTint)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(11.dp))
                Text(
                    text = stringResource(R.string.detalle_cliente_lista_negra_title),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ext.redText,
                )
            }
            if (result.balance > 0.0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Bs. %.2f".format(result.balance),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun MercadoResultRow(result: MercadoSearchResult, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PhotoThumbnail(
            photoUrl = result.photoUrl,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ext.surface3)
                .border(1.dp, ext.border, RoundedCornerShape(12.dp)),
            fallback = {
                Icon(Icons.Default.GridView, contentDescription = null, tint = ext.text2, modifier = Modifier.size(19.dp))
            },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(result.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.busqueda_mercado_clientes, result.clientesCount),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text3, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SearchField(
    query: String,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = ext.text2, modifier = Modifier.size(19.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.busqueda_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ext.text3,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery, modifier = Modifier.size(22.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(22.dp).clip(CircleShape).background(ext.surface3),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = ext.text2, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        },
    )
}
