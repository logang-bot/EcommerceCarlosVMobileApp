package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.CustomDateFields
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.GenerarPdfBar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.IntroText
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.PresetChipsRow
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.PreviewListSection
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.RangeSectionLabel
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.ResolvedDateBar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.SummaryCard
import com.restrusher.ecomercecarlosv.ui.screen.reporte.components.WarningBanner
import com.restrusher.ecomercecarlosv.presentation.screens.ReporteStatusRoute
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay
import com.restrusher.ecomercecarlosv.ui.screen.reporte.html.buildReporteClienteHtml
import com.restrusher.ecomercecarlosv.ui.screen.reporte.html.formatPeriodLabel
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteClienteScreen(navController: NavController) {
    val viewModel = hiltViewModel<ReporteClienteViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReporteClienteContent(
        state = state,
        onBack = { navController.popBackStack() },
        onSetPreset = viewModel::setPreset,
        onSetCustomFrom = viewModel::setCustomFrom,
        onSetCustomTo = viewModel::setCustomTo,
        onGenerarPdf = {
            val label = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())
            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            val safeName = state.clienteName.replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val fileName = "Reporte_${safeName}_$stamp.html"
            val html = buildReporteClienteHtml(state, formatPeriodLabel(state), label)
            ReporteExportHolder.pending = PendingExport(
                html = html,
                fileName = fileName,
                itemCount = state.pedidosCount,
                isMovimientosVariant = true,
            )
            navController.navigate(ReporteStatusRoute)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteClienteContent(
    state: ReporteClienteUiState,
    onBack: () -> Unit,
    onSetPreset: (ReporteClientePreset) -> Unit,
    onSetCustomFrom: (Long) -> Unit,
    onSetCustomTo: (Long) -> Unit,
    onGenerarPdf: () -> Unit,
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customFrom ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onSetCustomFrom)
                    showFromPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }

    if (showToPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customTo ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onSetCustomTo)
                    showToPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.reporte_generar_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                        )
                        if (state.clienteName.isNotBlank()) {
                            val subtitle = buildString {
                                append(state.clienteName)
                                if (state.mercadoName.isNotBlank()) append(" · ${state.mercadoName}")
                            }
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.extendedColors.text3,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            GenerarPdfBar(
                enabled = !state.isLoading &&
                    (state.preset != ReporteClientePreset.PERSONALIZADO || (state.fromMs > 0L && state.toMs > 0L)),
                onGenerarPdf = onGenerarPdf,
            )
        },
    ) { innerPadding ->
        LoadingOverlay(isLoading = state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            IntroText(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))

            RangeSectionLabel(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
            )

            PresetChipsRow(
                preset = state.preset,
                onSelect = onSetPreset,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(12.dp))

            if (state.preset == ReporteClientePreset.PERSONALIZADO) {
                CustomDateFields(
                    fromMs = state.customFrom,
                    toMs = state.customTo,
                    onFromClick = { showFromPicker = true },
                    onToClick = { showToPicker = true },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            } else {
                ResolvedDateBar(
                    state = state,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            SummaryCard(
                count = state.pedidosCount,
                total = state.montoTotal,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            if (state.showWarning) {
                Spacer(Modifier.height(12.dp))
                WarningBanner(modifier = Modifier.padding(horizontal = 20.dp))
            }

            Spacer(Modifier.height(20.dp))

            PreviewListSection(
                state = state,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(12.dp))
        }
        } // LoadingOverlay
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReporteClienteHoyPreview() {
    EcomerceCarlosVTheme {
        ReporteClienteContent(
            state = ReporteClienteUiState(
                clienteName = "Ana Rodríguez",
                mercadoName = "Mercado Central",
                preset = ReporteClientePreset.HOY,
                fromMs = System.currentTimeMillis(),
                toMs = System.currentTimeMillis(),
                pedidosCount = 2,
                montoTotal = 119.50,
                isLoading = false,
            ),
            onBack = {},
            onSetPreset = {},
            onSetCustomFrom = {},
            onSetCustomTo = {},
            onGenerarPdf = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ReporteClienteRangoDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ReporteClienteContent(
            state = ReporteClienteUiState(
                clienteName = "Carlos Mamani",
                mercadoName = "Mercado Sur",
                preset = ReporteClientePreset.MES,
                fromMs = System.currentTimeMillis() - 30L * 24 * 3_600_000,
                toMs = System.currentTimeMillis(),
                pedidosCount = 67,
                montoTotal = 4_230.00,
                showWarning = true,
                isLoading = false,
            ),
            onBack = {},
            onSetPreset = {},
            onSetCustomFrom = {},
            onSetCustomTo = {},
            onGenerarPdf = {},
        )
    }
}
