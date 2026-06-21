package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.GridView
import com.restrusher.ecomercecarlosv.ui.common.SyncBarIcon
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.presentation.screens.BusquedaRoute
import com.restrusher.ecomercecarlosv.presentation.screens.ListaNegraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.ClientesRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.PerfilRoute
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay
import com.restrusher.ecomercecarlosv.ui.common.RefreshErrorToast
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.PhotoThumbnail
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.screen.home.AppBottomNavBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun MercadosScreen(
    navController: NavController,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onSyncClick: () -> Unit = {},
    viewModel: MercadosViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MercadosContent(
        state = state,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onMercadoClick = { navController.navigate(ClientesRoute(it)) },
        onMercadoLongPress = viewModel::onMercadoLongPress,
        onCreateClick = { navController.navigate(CreateMercadoRoute()) },
        onVerDetallesClick = { mercadoId ->
            viewModel.clearSelection()
            navController.navigate(DetalleMercadoRoute(mercadoId))
        },
        onListaNegraClick = { navController.navigate(ListaNegraRoute) },
        onPerfilClick = { navController.navigate(PerfilRoute) },
        onSearchClick = { navController.navigate(BusquedaRoute) },
        onClearSelection = viewModel::clearSelection,
        onSyncClick = onSyncClick,
        onRefresh = viewModel::onRefresh,
        onRefreshErrorDismissed = viewModel::onRefreshErrorDismissed,
    )
}

@Composable
private fun LogoMark() {
    val ext = MaterialTheme.extendedColors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, ext.border2, CircleShape),
    ) {
        Image(
            painter = painterResource(R.drawable.img_logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MercadosContent(
    state: MercadosUiState,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onMercadoClick: (String) -> Unit,
    onMercadoLongPress: (String) -> Unit = {},
    onCreateClick: () -> Unit,
    onVerDetallesClick: (String) -> Unit = {},
    onListaNegraClick: () -> Unit,
    onPerfilClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onRefreshErrorDismissed: () -> Unit = {},
) {
    val count = state.mercados.size
    val isSelecting = state.selectedMercadoId != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AppBottomNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected) },
        topBar = {
            if (isSelecting) {
                ContextualActionBar(
                    onClose = onClearSelection,
                    onVerDetalles = { state.selectedMercadoId?.let(onVerDetallesClick) },
                )
            } else {
                PedidosTopBar(
                    title = stringResource(R.string.mercados_title),
                    subtitle = if (count > 0) stringResource(R.string.mercados_subtitle, count) else null,
                    large = true,
                    leading = { LogoMark() },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                        SyncBarIcon(state = state.syncIconState, onClick = onSyncClick)
                        IconButton(onClick = onPerfilClick) {
                            ProfileAvatar(initials = state.currentUserInitials.ifBlank { "?" }, photoUrl = state.currentUserPhotoUrl, size = 30)
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!isSelecting && state.canWrite) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.mercados_fab)) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = onCreateClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LoadingOverlay(isLoading = state.isLoading) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                ) {
                if (state.mercados.isEmpty() && !state.isLoading) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Default.GridView,
                        title = stringResource(R.string.mercados_empty_title),
                        subtitle = stringResource(R.string.mercados_empty_subtitle),
                        hint = stringResource(R.string.mercados_empty_hint),
                        onActionClick = onCreateClick,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp,
                        ),
                    ) {
                        if (isSelecting) {
                            item { SelectionHint() }
                        }
                        items(state.mercados, key = { it.id }) { mercado ->
                            val selected = mercado.id == state.selectedMercadoId
                            MercadoRow(
                                mercado = mercado,
                                stat = state.stats[mercado.id] ?: MercadoStat(),
                                selected = selected,
                                onClick = { onMercadoClick(mercado.id) },
                                onLongClick = { onMercadoLongPress(mercado.id) },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 78.dp),
                                color = MaterialTheme.extendedColors.border,
                            )
                        }
                        if (!isSelecting) {
                            item {
                                Spacer(Modifier.height(18.dp))
                                ListaNegraButton(onClick = onListaNegraClick)
                            }
                        }
                    }
                }
            }
        }
        if (state.refreshFailed) {
            RefreshErrorToast(
                onRetry = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 12.dp,
                    ),
            )
        }
        }
    }
}

@Composable
private fun ContextualActionBar(onClose: () -> Unit, onVerDetalles: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Surface(color = ext.accentSoft) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.mercados_n_seleccionado),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                )
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onVerDetalles)
                        .padding(start = 12.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text(
                        text = stringResource(R.string.mercados_accion_ver_detalles),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
            HorizontalDivider(color = ext.border)
        }
    }
}

@Composable
private fun SelectionHint() {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = ext.text3, modifier = Modifier.size(14.dp))
        Text(
            text = stringResource(R.string.mercados_selection_hint_v2),
            style = MaterialTheme.typography.bodySmall,
            color = ext.text3,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MercadoRow(
    modifier: Modifier = Modifier,
    mercado: Mercado,
    stat: MercadoStat,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val accentColor = MaterialTheme.colorScheme.primary
    val bgColor = if (selected) ext.accentSoft else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .drawBehind {
                drawRect(color = bgColor)
                if (selected) {
                    drawRect(color = accentColor, size = Size(3.dp.toPx(), size.height))
                }
            }
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
            MercadoTile(photoUrl = mercado.photoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mercado.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                MercadoStatRow(stat = stat)
            }
            if (selected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ext.text3,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
}

@Composable
private fun MercadoStatRow(stat: MercadoStat) {
    val ext = MaterialTheme.extendedColors
    val dotColor = when {
        stat.hasCritical -> ext.redText
        stat.hasWarning -> ext.amberText
        else -> null
    }
    val textColor = dotColor ?: ext.text2
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp),
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = stringResource(R.string.mercados_clientes_active, stat.activeClientCount),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
    }
}

@Composable
private fun MercadoTile(modifier: Modifier = Modifier, photoUrl: String? = null) {
    val ext = MaterialTheme.extendedColors
    PhotoThumbnail(
        photoUrl = photoUrl,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(ext.surface3)
            .border(1.dp, ext.border, RoundedCornerShape(13.dp)),
        fallback = {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = ext.text2,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

@Composable
private fun ListaNegraButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, ext.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ext.redTint),
        ) {
            Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.mercados_lista_negra_title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.mercados_lista_negra_subtitle), style = MaterialTheme.typography.bodySmall, color = ext.text2)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text3, modifier = Modifier.size(18.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MercadosScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        MercadosContent(MercadosUiState(currentUserInitials = "CV"), 0, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun MercadosScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        MercadosContent(MercadosUiState(currentUserInitials = "CV"), 0, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MercadosSelectionDarkPreview() {
    val mercados = listOf(
        Mercado(id = "m1", name = "Mercado de Coche", address = "", createdAt = 0L),
        Mercado(id = "m2", name = "Mercado Central", address = "", createdAt = 0L),
    )
    EcomerceCarlosVTheme(darkTheme = true) {
        MercadosContent(
            state = MercadosUiState(mercados = mercados, currentUserInitials = "CV", selectedMercadoId = "m1"),
            onMercadoClick = {}, onCreateClick = {}, onListaNegraClick = {},
        )
    }
}
