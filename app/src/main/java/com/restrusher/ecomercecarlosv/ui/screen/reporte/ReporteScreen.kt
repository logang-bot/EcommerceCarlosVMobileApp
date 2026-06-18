package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.screen.home.AppBottomNavBar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteDateChips
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteSelectorCard
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteSelectorSheet
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteStatCards
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.CobradoHeroCard
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.CustomDateRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.DiarioDateChips
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ReporteResolvedDateBar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.formatClienteBarText
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.formatDiarioBarText
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.DiarioStatCards
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.EmptyHistorial
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.EmptyMovimientos
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.HistorialRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.HistorialSectionHeader
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.MovimientoRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.MovimientosSectionHeader
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ReporteModeToggle
import com.restrusher.ecomercecarlosv.presentation.screens.ReporteStatusRoute
import com.restrusher.ecomercecarlosv.ui.screen.reporte.html.buildReporteHtml
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReporteScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController? = null,
    viewModel: ReporteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val onExportarPdf: () -> Unit = {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val isMovimientos = state.mode == ReporteMode.POR_CLIENTE
        val modeTag = if (state.mode == ReporteMode.DIARIO) "Diario" else "PorCliente"
        val fileName = "Reporte_${modeTag}_$stamp.html"
        val label = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())
        val html = buildReporteHtml(state, label)
        val itemCount = if (state.mode == ReporteMode.DIARIO) state.movimientos.size else state.historial.size
        ReporteExportHolder.pending = PendingExport(html, fileName, itemCount, isMovimientos)
        navController?.navigate(ReporteStatusRoute)
    }

    ReporteContent(
        state = state,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onSetMode = viewModel::setMode,
        onSetDiarioPreset = viewModel::setDiarioPreset,
        onSetClientePreset = viewModel::setClientePreset,
        onSetCustomDiarioFrom = viewModel::setCustomDiarioFrom,
        onSetCustomDiarioTo = viewModel::setCustomDiarioTo,
        onSetCustomClienteFrom = viewModel::setCustomClienteFrom,
        onSetCustomClienteTo = viewModel::setCustomClienteTo,
        onSelectCliente = viewModel::selectCliente,
        onExportarPdf = onExportarPdf,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteContent(
    state: ReporteUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSetMode: (ReporteMode) -> Unit,
    onSetDiarioPreset: (DiarioPreset) -> Unit,
    onSetClientePreset: (ClientePreset) -> Unit,
    onSetCustomDiarioFrom: (Long) -> Unit,
    onSetCustomDiarioTo: (Long) -> Unit,
    onSetCustomClienteFrom: (Long) -> Unit,
    onSetCustomClienteTo: (Long) -> Unit,
    onSelectCliente: (String) -> Unit,
    onExportarPdf: () -> Unit,
) {
    var showDiarioFromPicker by remember { mutableStateOf(false) }
    var showDiarioToPicker by remember { mutableStateOf(false) }
    var showClienteFromPicker by remember { mutableStateOf(false) }
    var showClienteToPicker by remember { mutableStateOf(false) }
    var showClienteSheet by remember { mutableStateOf(false) }

    if (showDiarioFromPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customDiarioFrom ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDiarioFromPicker = false },
            confirmButton = {
                TextButton(onClick = { pickerState.selectedDateMillis?.let(onSetCustomDiarioFrom); showDiarioFromPicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDiarioFromPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showDiarioToPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customDiarioTo ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDiarioToPicker = false },
            confirmButton = {
                TextButton(onClick = { pickerState.selectedDateMillis?.let(onSetCustomDiarioTo); showDiarioToPicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDiarioToPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showClienteFromPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customClienteFrom ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showClienteFromPicker = false },
            confirmButton = {
                TextButton(onClick = { pickerState.selectedDateMillis?.let(onSetCustomClienteFrom); showClienteFromPicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showClienteFromPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showClienteToPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customClienteTo ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showClienteToPicker = false },
            confirmButton = {
                TextButton(onClick = { pickerState.selectedDateMillis?.let(onSetCustomClienteTo); showClienteToPicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showClienteToPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showClienteSheet) {
        ClienteSelectorSheet(
            clientes = state.allClientes,
            selectedId = state.selectedClienteId,
            onSelect = { id -> onSelectCliente(id); showClienteSheet = false },
            onDismiss = { showClienteSheet = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.nav_reporte),
                actions = {
                    IconButton(onClick = onExportarPdf) {
                        Icon(Icons.Default.Description, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            AppBottomNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
        },
    ) { innerPadding ->
        LoadingOverlay(isLoading = state.isLoading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                ReporteModeToggle(mode = state.mode, onSetMode = onSetMode)
                Spacer(Modifier.height(16.dp))
            }

            if (state.mode == ReporteMode.DIARIO) {
                item {
                    DiarioDateChips(preset = state.diarioPreset, onSelect = onSetDiarioPreset)
                    Spacer(Modifier.height(10.dp))
                    if (state.diarioPreset == DiarioPreset.PERSONALIZADO) {
                        CustomDateRow(
                            fromMs = state.customDiarioFrom,
                            toMs = state.customDiarioTo,
                            onFromClick = { showDiarioFromPicker = true },
                            onToClick = { showDiarioToPicker = true },
                        )
                    } else {
                        ReporteResolvedDateBar(
                            text = formatDiarioBarText(state.diarioPreset, state.diarioFromMs, state.diarioToMs),
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    CobradoHeroCard(state = state)
                    Spacer(Modifier.height(12.dp))
                    DiarioStatCards(state = state)
                    Spacer(Modifier.height(20.dp))
                    MovimientosSectionHeader()
                }
                if (state.movimientos.isEmpty()) {
                    item { EmptyMovimientos() }
                } else {
                    items(state.movimientos, key = { "${it.pedidoId}_${it.type}" }) { mov ->
                        MovimientoRow(item = mov)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 54.dp, end = 20.dp),
                            color = MaterialTheme.extendedColors.border,
                        )
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            } else {
                item {
                    ClienteDateChips(preset = state.clientePreset, onSelect = onSetClientePreset)
                    Spacer(Modifier.height(10.dp))
                    if (state.clientePreset == ClientePreset.PERSONALIZADO) {
                        CustomDateRow(
                            fromMs = state.customClienteFrom,
                            toMs = state.customClienteTo,
                            onFromClick = { showClienteFromPicker = true },
                            onToClick = { showClienteToPicker = true },
                        )
                    } else {
                        ReporteResolvedDateBar(
                            text = formatClienteBarText(state.clientePreset, state.clienteFromMs, state.clienteToMs),
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    if (state.allClientes.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Person,
                            title = "Sin clientes",
                            subtitle = "Agrega clientes para ver reportes por cliente",
                        )
                    } else {
                        ClienteSelectorCard(
                            name = state.selectedClienteName,
                            photoUrl = state.selectedClientePhotoUrl,
                            mercadoName = state.selectedMercadoName,
                            onCambiar = { showClienteSheet = true },
                        )
                        Spacer(Modifier.height(12.dp))
                        ClienteStatCards(state = state)
                        Spacer(Modifier.height(20.dp))
                        HistorialSectionHeader()
                    }
                }
                if (state.allClientes.isNotEmpty()) {
                    if (state.historial.isEmpty()) {
                        item { EmptyHistorial() }
                    } else {
                        items(state.historial, key = { it.pedidoId }) { item ->
                            HistorialRow(item = item)
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                                color = MaterialTheme.extendedColors.border,
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
        } // LoadingOverlay
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewDiarioState = ReporteUiState(
    mode = ReporteMode.DIARIO,
    diarioPreset = DiarioPreset.HOY,
    cobradoTotal = 617.50,
    cobroCount = 9,
    pedidosCreadosCount = 14,
    pendienteDelDia = 238.0,
    movimientos = listOf(
        MovimientoItem("1", "Doris Salazar", "Mercado Central", MovimientoType.COBRO, 85.50, System.currentTimeMillis()),
        MovimientoItem("2", "María González", "Mercado Central", MovimientoType.PEDIDO, 47.50, System.currentTimeMillis()),
    ),
    isLoading = false,
)

private val previewClienteState = ReporteUiState(
    mode = ReporteMode.POR_CLIENTE,
    clientePreset = ClientePreset.MES,
    selectedClienteName = "Ana Rodríguez",
    selectedMercadoName = "Mercado Central",
    facturado = 284.0,
    pagado = 164.0,
    saldo = 120.0,
    allClientes = listOf(ClienteOption("1", "Ana Rodríguez", null, "Mercado Central")),
    historial = listOf(
        HistorialItem("1", "12 Jun 2026", System.currentTimeMillis(), 88.0, 0.0, 88.0, false),
        HistorialItem("2", "08 Jun 2026", System.currentTimeMillis(), 110.0, 110.0, 0.0, false),
    ),
    isLoading = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReporteDiarioPreview() {
    EcomerceCarlosVTheme {
        ReporteContent(
            state = previewDiarioState,
            selectedTab = 2,
            onTabSelected = {},
            onSetMode = {},
            onSetDiarioPreset = {},
            onSetClientePreset = {},
            onSetCustomDiarioFrom = {},
            onSetCustomDiarioTo = {},
            onSetCustomClienteFrom = {},
            onSetCustomClienteTo = {},
            onSelectCliente = {},
            onExportarPdf = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ReportePorClienteDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ReporteContent(
            state = previewClienteState,
            selectedTab = 2,
            onTabSelected = {},
            onSetMode = {},
            onSetDiarioPreset = {},
            onSetClientePreset = {},
            onSetCustomDiarioFrom = {},
            onSetCustomDiarioTo = {},
            onSetCustomClienteFrom = {},
            onSetCustomClienteTo = {},
            onSelectCliente = {},
            onExportarPdf = {},
        )
    }
}
