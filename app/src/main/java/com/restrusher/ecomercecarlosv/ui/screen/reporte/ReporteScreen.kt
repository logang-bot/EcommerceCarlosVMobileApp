package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import android.util.Base64
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.PedidoLineItem
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.screen.home.AppBottomNavBar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteDateChips
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteSelectorCard
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ClienteSelectorSheet
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.CustomDateRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.DiarioDateChips
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.FacturadoHeroCard
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.PagadoPorPagarRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ReporteResolvedDateBar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.formatClienteBarText
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.formatDiarioBarText
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.EmptyHistorial
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.HistorialRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.HistorialSectionHeader
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.PedidosSaldoExtraCards
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
    val context = LocalContext.current
    val logoDataUri = remember {
        val bytes = context.resources.openRawResource(R.drawable.img_logo).use { it.readBytes() }
        "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    val onExportarPdf: () -> Unit = {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val modeTag = if (state.mode == ReporteMode.DIARIO) "Diario" else "PorCliente"
        val fileName = "Reporte_${modeTag}_$stamp.html"
        val label = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())
        val html = buildReporteHtml(state, label, logoDataUri)
        val itemCount = if (state.mode == ReporteMode.DIARIO) {
            state.diarioPedidos.size
        } else {
            state.historial.size + state.saldoExtras.size
        }
        ReporteExportHolder.pending = PendingExport(html, fileName, itemCount, isMovimientosVariant = false)
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
                    Button(
                        onClick = onExportarPdf,
                        modifier = Modifier
                            .height(38.dp)
                            .padding(end = 12.dp),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(start = 12.dp, end = 14.dp),
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = stringResource(R.string.reporte_pdf_chip),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                    FacturadoHeroCard(
                        total = state.diarioFacturado,
                        pedidosCount = state.diarioPedidos.size,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    PagadoPorPagarRow(
                        pagado = state.diarioPagado,
                        porPagar = state.diarioPendiente,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                    HistorialSectionHeader(
                        title = stringResource(R.string.reporte_seccion_pedidos),
                        action = stringResource(R.string.reporte_pedidos_count, state.diarioPedidos.size),
                    )
                }
                if (state.diarioPedidos.isEmpty()) {
                    item { EmptyHistorial() }
                } else {
                    items(state.diarioPedidos, key = { it.pedidoId }) { item ->
                        HistorialRow(item = item)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 20.dp),
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
                        PedidosSaldoExtraCards(
                            pedidosCount = state.historial.size,
                            saldoExtra = state.saldoExtras.sumOf { it.pending },
                        )
                        Spacer(Modifier.height(20.dp))
                        HistorialSectionHeader(
                            title = stringResource(R.string.reporte_historial),
                            action = stringResource(R.string.reporte_pedidos_count, state.historial.size),
                        )
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
                    if (state.saldoExtras.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            HistorialSectionHeader(
                                title = stringResource(R.string.pedidos_row_saldo_extra),
                                action = stringResource(R.string.reporte_pedidos_count, state.saldoExtras.size),
                            )
                        }
                        items(state.saldoExtras, key = { it.pedidoId }) { item ->
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
    diarioFacturado = 855.50,
    diarioPagado = 617.50,
    diarioPendiente = 238.0,
    diarioPedidos = listOf(
        HistorialItem("1", "Doris Salazar", System.currentTimeMillis(), 85.50, 85.50, 0.0, false, subtitle = "7 Jul, 14:32 · Mercado Central"),
        HistorialItem("2", "María González", System.currentTimeMillis(), 47.50, 0.0, 47.50, false, subtitle = "7 Jul, 11:05 · Mercado Central"),
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
        HistorialItem(
            "1", "12 Jun 2026", System.currentTimeMillis(), 88.0, 0.0, 88.0, false,
            lines = listOf(PedidoLineItem("Tomate perita", 5), PedidoLineItem("Cebolla blanca", 4)),
        ),
        HistorialItem(
            "2", "08 Jun 2026", System.currentTimeMillis(), 110.0, 110.0, 0.0, false,
            lines = listOf(PedidoLineItem("Harina PAN 1kg", 12)),
        ),
    ),
    saldoExtras = listOf(
        HistorialItem(
            "3", "Saldo extra", System.currentTimeMillis(), 45.0, 0.0, 45.0, true,
            subtitle = "15 May 2026 · Envases retornables",
        ),
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
