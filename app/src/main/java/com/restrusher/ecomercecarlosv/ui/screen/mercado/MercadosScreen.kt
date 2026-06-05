package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.PerfilRoute
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.screen.home.AppBottomNavBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun MercadosScreen(
    navController: NavController,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    viewModel: MercadosViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MercadosContent(
        state = state,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onMercadoClick = { navController.navigate(DetalleMercadoRoute(it)) },
        onCreateClick = { navController.navigate(CreateMercadoRoute()) },
        onListaNegraClick = { /* TODO: navigate to ListaNegraRoute — Phase 7 */ },
        onPerfilClick = { navController.navigate(PerfilRoute) },
    )
}

@Composable
private fun MercadosContent(
    state: MercadosUiState,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onMercadoClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onListaNegraClick: () -> Unit,
    onPerfilClick: () -> Unit = {},
) {
    val count = state.mercados.size
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AppBottomNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected) },
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.mercados_title),
                subtitle = if (count > 0) stringResource(R.string.mercados_subtitle, count) else null,
                large = true,
                actions = {
                    IconButton(onClick = { /* TODO: BusquedaRoute */ }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                    IconButton(onClick = onPerfilClick) {
                        ProfileAvatar(initials = state.currentUserInitials.ifBlank { "?" }, size = 30)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.mercados_fab)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            )
        },
    ) { innerPadding ->
        if (state.mercados.isEmpty() && !state.isLoading) {
            EmptyState(
                modifier = Modifier.padding(innerPadding),
                icon = Icons.Default.GridView,
                title = stringResource(R.string.mercados_empty_title),
                subtitle = stringResource(R.string.mercados_empty_subtitle),
                hint = stringResource(R.string.mercados_empty_hint),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                items(state.mercados, key = { it.id }) { mercado ->
                    MercadoRow(mercado = mercado, onClick = { onMercadoClick(mercado.id) })
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 78.dp),
                        color = MaterialTheme.extendedColors.border,
                    )
                }
                item {
                    Spacer(Modifier.height(18.dp))
                    ListaNegraButton(onClick = onListaNegraClick)
                }
            }
        }
    }
}

@Composable
private fun MercadoRow(modifier: Modifier = Modifier, mercado: Mercado, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MercadoTile(initial = mercado.name.firstOrNull()?.uppercase() ?: "M")
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mercado.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.mercados_clientes_active, 0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.extendedColors.text2,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.extendedColors.text3,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun MercadoTile(modifier: Modifier = Modifier, initial: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Text(initial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ListaNegraButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.extendedColors.redTint),
        ) {
            Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.extendedColors.redText, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.mercados_lista_negra_title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.mercados_lista_negra_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extendedColors.text2)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.extendedColors.text3, modifier = Modifier.size(18.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MercadosScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        MercadosContent(MercadosUiState(currentUserInitials = "CV"), 0, {}, {}, {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun MercadosScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        MercadosContent(MercadosUiState(currentUserInitials = "CV"), 0, {}, {}, {}, {}, {})
    }
}
