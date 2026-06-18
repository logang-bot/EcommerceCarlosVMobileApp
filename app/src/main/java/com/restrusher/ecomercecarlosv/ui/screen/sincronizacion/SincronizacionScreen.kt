package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.SyncIconState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun SincronizacionScreen(
    navController: NavController,
    viewModel: SincronizacionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SincronizacionScreenContent(
        state = state,
        onRetry = viewModel::onRetry,
        onBack = { navController.popBackStack() },
    )
}

@Composable
private fun SincronizacionScreenContent(
    state: SincronizacionUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.sinc_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        when (state.syncState) {
            SyncIconState.SYNCED -> SyncDone(
                lastSyncedAt = state.lastSyncedAt,
                modifier = Modifier.padding(innerPadding),
            )
            SyncIconState.PENDING, SyncIconState.ERROR -> SyncPendingContent(
                state = state,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun SyncPendingContent(
    state: SincronizacionUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SyncBanner(state = state, onRetry = onRetry) }
        item {
            SectionLabel(text = stringResource(R.string.sinc_section_label, state.items.size))
        }
        itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
            SyncRow(item = item, isError = state.syncState == SyncIconState.ERROR)
            if (index < state.items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 71.dp),
                    color = MaterialTheme.extendedColors.border,
                )
            }
        }
        item { FooterNote() }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val previewItems = listOf(
    SyncQueueItem(
        id = 1,
        entityType = EntityType.PEDIDO,
        operation = SyncOp.UPSERT,
        entityLabel = "Bs. 1.240,00",
        createdAt = System.currentTimeMillis() - 2 * 60_000,
        retryCount = 0,
    ),
    SyncQueueItem(
        id = 2,
        entityType = EntityType.CLIENTE,
        operation = SyncOp.UPSERT,
        entityLabel = "María González",
        createdAt = System.currentTimeMillis() - 4 * 60_000,
        retryCount = 0,
    ),
    SyncQueueItem(
        id = 3,
        entityType = EntityType.PEDIDO,
        operation = SyncOp.UPSERT,
        entityLabel = "Bs. 560,00",
        createdAt = System.currentTimeMillis() - 9 * 60_000,
        retryCount = 0,
    ),
    SyncQueueItem(
        id = 4,
        entityType = EntityType.MERCADO,
        operation = SyncOp.UPSERT,
        entityLabel = "Mercado de Coche",
        createdAt = System.currentTimeMillis() - 14 * 60_000,
        retryCount = 0,
    ),
)

private val syncedState = SincronizacionUiState(
    syncState = SyncIconState.SYNCED,
    items = emptyList(),
    lastSyncedAt = System.currentTimeMillis() - 2 * 60_000,
)

private val pendingState = SincronizacionUiState(
    syncState = SyncIconState.PENDING,
    items = previewItems,
    lastSyncedAt = System.currentTimeMillis() - 15 * 60_000,
)

private val errorState = SincronizacionUiState(
    syncState = SyncIconState.ERROR,
    items = previewItems.map { it.copy(retryCount = 1) },
    lastSyncedAt = System.currentTimeMillis() - 15 * 60_000,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Sincronizacion Synced Light")
@Composable
private fun SincronizacionSyncedLightPreview() {
    EcomerceCarlosVTheme {
        Surface { SincronizacionScreenContent(state = syncedState, onRetry = {}, onBack = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Sincronizacion Synced Dark")
@Composable
private fun SincronizacionSyncedDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SincronizacionScreenContent(state = syncedState, onRetry = {}, onBack = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Sincronizacion Pending Light")
@Composable
private fun SincronizacionPendingLightPreview() {
    EcomerceCarlosVTheme {
        Surface { SincronizacionScreenContent(state = pendingState, onRetry = {}, onBack = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Sincronizacion Pending Dark")
@Composable
private fun SincronizacionPendingDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SincronizacionScreenContent(state = pendingState, onRetry = {}, onBack = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Sincronizacion Error Light")
@Composable
private fun SincronizacionErrorLightPreview() {
    EcomerceCarlosVTheme {
        Surface { SincronizacionScreenContent(state = errorState, onRetry = {}, onBack = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Sincronizacion Error Dark")
@Composable
private fun SincronizacionErrorDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SincronizacionScreenContent(state = errorState, onRetry = {}, onBack = {}) }
    }
}
