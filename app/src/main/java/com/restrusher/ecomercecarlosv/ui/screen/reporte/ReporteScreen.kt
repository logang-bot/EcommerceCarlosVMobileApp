package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.AppBottomNavBar
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReporteScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: ReporteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val onExportarPdf: () -> Unit = {
        val dateStr = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())
        val html = buildReporteHtml(state, dateStr)
        val webView = WebView(context)
        webView.settings.javaScriptEnabled = false
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Reporte_${System.currentTimeMillis()}"
                pm.print(jobName, view.createPrintDocumentAdapter(jobName), PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
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
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onSetCustomDiarioFrom)
                    showDiarioFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDiarioFromPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showDiarioToPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customDiarioTo ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDiarioToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onSetCustomDiarioTo)
                    showDiarioToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDiarioToPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showClienteFromPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customClienteFrom ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showClienteFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onSetCustomClienteFrom)
                    showClienteFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showClienteFromPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }
    if (showClienteToPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.customClienteTo ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showClienteToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onSetCustomClienteTo)
                    showClienteToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showClienteToPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }

    if (showClienteSheet) {
        ClienteSelectorSheet(
            clientes = state.allClientes,
            selectedId = state.selectedClienteId,
            onSelect = { id ->
                onSelectCliente(id)
                showClienteSheet = false
            },
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
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
                    if (state.diarioPreset == DiarioPreset.PERSONALIZADO) {
                        Spacer(Modifier.height(10.dp))
                        CustomDateRow(
                            fromMs = state.customDiarioFrom,
                            toMs = state.customDiarioTo,
                            onFromClick = { showDiarioFromPicker = true },
                            onToClick = { showDiarioToPicker = true },
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
                    item {
                        EmptyMovimientos()
                    }
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
                    if (state.clientePreset == ClientePreset.PERSONALIZADO) {
                        Spacer(Modifier.height(10.dp))
                        CustomDateRow(
                            fromMs = state.customClienteFrom,
                            toMs = state.customClienteTo,
                            onFromClick = { showClienteFromPicker = true },
                            onToClick = { showClienteToPicker = true },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    if (state.allClientes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Icons.Default.Person,
                                title = "Sin clientes",
                                subtitle = "Agrega clientes para ver reportes por cliente",
                            )
                        }
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
    }
}

// ── Mode toggle ───────────────────────────────────────────────────────────────

@Composable
private fun ReporteModeToggle(mode: ReporteMode, onSetMode: (ReporteMode) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(13.dp))
            .padding(3.dp),
    ) {
        ModeButton(
            label = stringResource(R.string.reporte_mode_diario),
            selected = mode == ReporteMode.DIARIO,
            modifier = Modifier.weight(1f),
            onClick = { onSetMode(ReporteMode.DIARIO) },
        )
        ModeButton(
            label = stringResource(R.string.reporte_mode_por_cliente),
            selected = mode == ReporteMode.POR_CLIENTE,
            modifier = Modifier.weight(1f),
            onClick = { onSetMode(ReporteMode.POR_CLIENTE) },
        )
    }
}

@Composable
private fun ModeButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.extendedColors.text3,
        )
    }
}

// ── Date chips ────────────────────────────────────────────────────────────────

@Composable
private fun DiarioDateChips(preset: DiarioPreset, onSelect: (DiarioPreset) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DiarioPreset.entries.forEach { p ->
            DateChip(
                label = when (p) {
                    DiarioPreset.HOY -> stringResource(R.string.reporte_preset_hoy)
                    DiarioPreset.AYER -> stringResource(R.string.reporte_preset_ayer)
                    DiarioPreset.SEMANA -> stringResource(R.string.reporte_preset_semana)
                    DiarioPreset.PERSONALIZADO -> stringResource(R.string.reporte_preset_personalizado)
                },
                selected = preset == p,
                onClick = { onSelect(p) },
            )
        }
    }
}

@Composable
private fun ClienteDateChips(preset: ClientePreset, onSelect: (ClientePreset) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ClientePreset.entries.forEach { p ->
            DateChip(
                label = when (p) {
                    ClientePreset.MES -> stringResource(R.string.reporte_preset_mes)
                    ClientePreset.TRIMESTRE -> stringResource(R.string.reporte_preset_trimestre)
                    ClientePreset.ANIO -> stringResource(R.string.reporte_preset_anio)
                    ClientePreset.PERSONALIZADO -> stringResource(R.string.reporte_preset_personalizado)
                },
                selected = preset == p,
                onClick = { onSelect(p) },
            )
        }
    }
}

@Composable
private fun DateChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) ext.accentSoft else ext.surface2)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else ext.border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else ext.text2,
        )
    }
}

// ── Personalizado date row ────────────────────────────────────────────────────

@Composable
private fun CustomDateRow(
    fromMs: Long?,
    toMs: Long?,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
) {
    val df = remember { SimpleDateFormat("d MMM yyyy", Locale("es")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DateField(
            label = stringResource(R.string.reporte_desde),
            value = fromMs?.let { df.format(Date(it)) } ?: "—",
            onClick = onFromClick,
            modifier = Modifier.weight(1f),
        )
        DateField(
            label = stringResource(R.string.reporte_hasta),
            value = toMs?.let { df.format(Date(it)) } ?: "—",
            onClick = onToClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DateField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = ext.text3,
            letterSpacing = 0.3.sp,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ext.text3, modifier = Modifier.size(14.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (value == "—") ext.text4 else MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Cobrado hero card ─────────────────────────────────────────────────────────

@Composable
private fun CobradoHeroCard(state: ReporteUiState) {
    val ext = MaterialTheme.extendedColors
    val greenColor = ext.green
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(greenColor.copy(alpha = 0.16f), greenColor.copy(alpha = 0.05f)),
                ),
            )
            .border(1.dp, greenColor.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .padding(horizontal = 22.dp, vertical = 22.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.reporte_cobrado_hoy),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ext.greenText,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Bs. ${"%.2f".format(state.cobradoTotal)}",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.reporte_cobros_count, state.cobroCount),
                fontSize = 13.sp,
                color = ext.text3,
            )
        }
    }
}

// ── Stat cards ────────────────────────────────────────────────────────────────

@Composable
private fun DiarioStatCards(state: ReporteUiState) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ReporteStatCard(
            label = stringResource(R.string.reporte_pedidos_creados),
            value = state.pedidosCreadosCount.toString(),
            valueColor = MaterialTheme.colorScheme.primary,
            bgColor = ext.accentSoft,
            modifier = Modifier.weight(1f),
        )
        ReporteStatCard(
            label = stringResource(R.string.reporte_pendiente_dia),
            value = "Bs. ${"%.2f".format(state.pendienteDelDia)}",
            valueColor = ext.amberText,
            bgColor = ext.amberTint,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ClienteStatCards(state: ReporteUiState) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReporteStatCard(
            label = stringResource(R.string.reporte_facturado),
            value = "Bs. ${"%.2f".format(state.facturado)}",
            valueColor = MaterialTheme.colorScheme.primary,
            bgColor = ext.accentSoft,
            modifier = Modifier.weight(1f),
        )
        ReporteStatCard(
            label = stringResource(R.string.reporte_pagado),
            value = "Bs. ${"%.2f".format(state.pagado)}",
            valueColor = ext.greenText,
            bgColor = ext.greenTint,
            modifier = Modifier.weight(1f),
        )
        ReporteStatCard(
            label = stringResource(R.string.reporte_saldo),
            value = "Bs. ${"%.2f".format(state.saldo)}",
            valueColor = ext.amberText,
            bgColor = ext.amberTint,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReporteStatCard(
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.extendedColors.text3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.3).sp,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Movimientos section ───────────────────────────────────────────────────────

@Composable
private fun MovimientosSectionHeader() {
    val ext = MaterialTheme.extendedColors
    Text(
        text = stringResource(R.string.reporte_movimientos).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = ext.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun EmptyMovimientos() {
    Text(
        text = stringResource(R.string.reporte_sin_movimientos),
        fontSize = 13.sp,
        color = MaterialTheme.extendedColors.text3,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    )
}

@Composable
private fun MovimientoRow(item: MovimientoItem) {
    val ext = MaterialTheme.extendedColors
    val dotColor = if (item.type == MovimientoType.COBRO) ext.greenText else MaterialTheme.colorScheme.primary
    val timeStr = remember(item.timestamp) {
        SimpleDateFormat("HH:mm", Locale("es")).format(Date(item.timestamp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.clienteName.ifBlank { "—" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildString {
                    append(if (item.type == MovimientoType.COBRO) "Cobro" else "Pedido")
                    if (item.mercadoName.isNotBlank()) append(" · ${item.mercadoName}")
                    append(" · $timeStr")
                },
                fontSize = 12.sp,
                color = ext.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "Bs. ${"%.2f".format(item.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = if (item.type == MovimientoType.COBRO) ext.greenText else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Client selector ───────────────────────────────────────────────────────────

@Composable
private fun ClienteSelectorCard(
    name: String,
    photoUrl: String?,
    mercadoName: String,
    onCambiar: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ClienteAvatar(name = name, photoUrl = photoUrl, size = 44.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.ifBlank { stringResource(R.string.reporte_selector_cliente) },
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (mercadoName.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(text = mercadoName, fontSize = 12.sp, color = ext.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(ext.accentSoft)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(9.dp))
                .clickable(onClick = onCambiar)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.reporte_cambiar_cliente),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClienteSelectorSheet(
    clientes: List<ClienteOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.reporte_selector_cliente),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
            HorizontalDivider(color = ext.border)
            clientes.forEach { cliente ->
                val isSelected = cliente.id == selectedId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) ext.accentSoft else Color.Transparent)
                        .clickable { onSelect(cliente.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ClienteAvatar(name = cliente.name, photoUrl = cliente.photoUrl, size = 38.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cliente.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (cliente.mercadoName.isNotBlank()) {
                            Text(cliente.mercadoName, fontSize = 12.sp, color = ext.text2, maxLines = 1)
                        }
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(start = 70.dp), color = ext.border)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Historial section ─────────────────────────────────────────────────────────

@Composable
private fun HistorialSectionHeader() {
    val ext = MaterialTheme.extendedColors
    Text(
        text = stringResource(R.string.reporte_historial).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = ext.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun EmptyHistorial() {
    Text(
        text = stringResource(R.string.reporte_sin_historial),
        fontSize = 13.sp,
        color = MaterialTheme.extendedColors.text3,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    )
}

@Composable
private fun HistorialRow(item: HistorialItem) {
    val ext = MaterialTheme.extendedColors
    val (iconVec, iconBg, iconTint) = historialIconSpec(item, ext)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
        ) {
            Icon(iconVec, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (item.pending > 0) "Bs. ${"%.2f".format(item.pending)} pendiente" else "Pagado",
                fontSize = 12.sp,
                color = if (item.pending > 0) ext.amberText else ext.greenText,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Bs. ${"%.2f".format(item.total)}",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                letterSpacing = (-0.3).sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (item.isSaldoExtra) "Extra" else "Total",
                fontSize = 11.sp,
                color = ext.text3,
            )
        }
    }
}

private data class IconSpec(val icon: ImageVector, val bg: Color, val tint: Color)

@Composable
private fun historialIconSpec(item: HistorialItem, ext: com.restrusher.ecomercecarlosv.ui.theme.PedidosExtendedColors): IconSpec {
    return when {
        item.isSaldoExtra -> IconSpec(Icons.Default.Assessment, ext.amberTint, ext.amberText)
        item.pending == 0.0 -> IconSpec(Icons.Default.Check, ext.greenTint, ext.greenText)
        else -> IconSpec(Icons.Default.Receipt, ext.accentSoft, MaterialTheme.colorScheme.primary)
    }
}

// ── HTML builders ─────────────────────────────────────────────────────────────

private fun buildReporteHtml(state: ReporteUiState, generatedDate: String): String {
    return if (state.mode == ReporteMode.DIARIO) buildDiarioHtml(state, generatedDate)
    else buildClienteHtml(state, generatedDate)
}

private fun buildDiarioHtml(state: ReporteUiState, generatedDate: String): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val fromStr = df.format(Date(state.diarioFromMs))
    val toStr = df.format(Date(state.diarioToMs))
    val rangeStr = if (state.diarioFromMs == state.diarioToMs || fromStr == toStr) fromStr else "$fromStr – $toStr"

    val movRows = state.movimientos.joinToString("") { mov ->
        val typeLabel = if (mov.type == MovimientoType.COBRO) "Cobro" else "Pedido"
        val typeClass = if (mov.type == MovimientoType.COBRO) "paid" else "pending"
        val timeStr = SimpleDateFormat("HH:mm", Locale("es")).format(Date(mov.timestamp))
        "<tr><td><b>${mov.clienteName.ifBlank { "—" }}</b><div class='sub'>$typeLabel · ${mov.mercadoName} · $timeStr</div></td>" +
            "<td><span class='chip $typeClass'>$typeLabel</span></td>" +
            "<td class='amt'>Bs. ${"%.2f".format(mov.amount)}</td></tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
h2{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#aaa;margin:20px 0 8px}
.sum{display:flex;gap:10px;margin:8px 0}
.sc{flex:1;border-radius:9px;padding:11px 13px}
.sc.g{background:#F2FBF6;border:1px solid #B3E9C7}.sc.g .sv{color:#16a34a}
.sc.a{background:#FFF8E6;border:1px solid #FAE09A}.sc.a .sv{color:#D97706}
.sc.b{background:#EEF4FF;border:1px solid #C3D3F7}.sc.b .sv{color:#4C8DF5}
.sl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#888;margin-bottom:4px}
.sv{font-size:20px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:4px}
th{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:6px 10px;border-bottom:1px solid #eee;text-align:left}
td{padding:9px 10px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.sub{font-size:10.5px;color:#999;margin-top:3px}
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.chip{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10.5px;font-weight:600;white-space:nowrap}
.chip.paid{background:#F2FBF6;color:#16a34a}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <div><h1>Reporte diario</h1><div class="meta">Comercializadora Carlos V &middot; $rangeStr</div></div>
  <div class="meta" style="text-align:right">$generatedDate</div>
</div>
<h2>Resumen</h2>
<div class="sum">
  <div class="sc g"><div class="sl">Cobrado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.cobradoTotal)}</div></div>
  <div class="sc b"><div class="sl">Pedidos creados</div><div class="sv">${state.pedidosCreadosCount}</div></div>
  <div class="sc a"><div class="sl">Pendiente del día</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.pendienteDelDia)}</div></div>
</div>
<h2>Movimientos</h2>
${if (movRows.isEmpty()) "<p style='color:#aaa;font-size:12px'>Sin movimientos en este período.</p>"
else "<table><thead><tr><th>Cliente</th><th>Tipo</th><th style='text-align:right'>Monto</th></tr></thead><tbody>$movRows</tbody></table>"}
<div class="footer">Comercializadora Carlos V &middot; Generado el $generatedDate</div>
</body></html>"""
}

private fun buildClienteHtml(state: ReporteUiState, generatedDate: String): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val fromStr = df.format(Date(state.clienteFromMs))
    val toStr = df.format(Date(state.clienteToMs))

    val historialRows = state.historial.joinToString("") { item ->
        val statusLabel = when {
            item.isSaldoExtra -> "Extra"
            item.pending == 0.0 -> "Pagado"
            item.paid > 0 -> "Parcial"
            else -> "Pendiente"
        }
        val statusClass = when {
            item.isSaldoExtra || (item.paid > 0 && item.pending > 0) -> "partial"
            item.pending == 0.0 -> "paid"
            else -> "pending"
        }
        val pendingCell = if (item.pending > 0)
            "<td class='amt amber'>Bs. ${"%.2f".format(item.pending)}</td>"
        else "<td class='amt gray'>—</td>"
        "<tr><td><b>${item.title}</b></td><td><span class='chip $statusClass'>$statusLabel</span></td>" +
            "<td class='amt'>Bs. ${"%.2f".format(item.total)}</td>$pendingCell</tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
h2{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#aaa;margin:20px 0 8px}
.sum{display:flex;gap:10px;margin:8px 0}
.sc{flex:1;border-radius:9px;padding:11px 13px}
.sc.g{background:#F2FBF6;border:1px solid #B3E9C7}.sc.g .sv{color:#16a34a}
.sc.a{background:#FFF8E6;border:1px solid #FAE09A}.sc.a .sv{color:#D97706}
.sc.p{background:#EEF4FF;border:1px solid #C3D3F7}.sc.p .sv{color:#4C8DF5}
.sl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#888;margin-bottom:4px}
.sv{font-size:20px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:4px}
th{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:6px 10px;border-bottom:1px solid #eee;text-align:left}
td{padding:9px 10px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.amber{color:#D97706}.gray{color:#ccc}
.chip{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10.5px;font-weight:600;white-space:nowrap}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.chip.partial{background:#FFF8E6;color:#D97706}
.chip.paid{background:#F2FBF6;color:#16a34a}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <div><h1>Reporte por cliente</h1><div class="meta">Comercializadora Carlos V &middot; ${state.selectedClienteName} &middot; ${state.selectedMercadoName}</div></div>
  <div class="meta" style="text-align:right">$fromStr – $toStr<br>$generatedDate</div>
</div>
<h2>Resumen</h2>
<div class="sum">
  <div class="sc p"><div class="sl">Facturado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.facturado)}</div></div>
  <div class="sc g"><div class="sl">Pagado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.pagado)}</div></div>
  <div class="sc a"><div class="sl">Saldo</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.saldo)}</div></div>
</div>
<h2>Historial</h2>
${if (historialRows.isEmpty()) "<p style='color:#aaa;font-size:12px'>Sin transacciones en este período.</p>"
else "<table><thead><tr><th>Pedido</th><th>Estado</th><th style='text-align:right'>Total</th><th style='text-align:right'>Pendiente</th></tr></thead><tbody>$historialRows</tbody></table>"}
<div class="footer">Comercializadora Carlos V &middot; ${state.selectedClienteName} &middot; Generado el $generatedDate</div>
</body></html>"""
}
