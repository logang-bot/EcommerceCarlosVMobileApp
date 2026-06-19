package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DepuracionScreen(
    navController: NavController,
    viewModel: DepuracionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.phase == DepuracionPhase.EXPORTING || state.phase == DepuracionPhase.DELETING) {
        // Block back during active operations
    }

    when (state.phase) {
        DepuracionPhase.CONFIG -> DepuracionConfigContent(
            state = state,
            onBack = { navController.popBackStack() },
            onCutoffClick = viewModel::onShowDatePicker,
            onFormatSelected = viewModel::onFormatSelected,
            onExportarClick = viewModel::onExportarClick,
        )
        DepuracionPhase.EXPORTING -> DepuracionProgressContent(
            state = state,
            isExporting = true,
        )
        DepuracionPhase.DELETING -> DepuracionProgressContent(
            state = state,
            isExporting = false,
        )
        DepuracionPhase.DONE -> DepuracionDoneContent(
            state = state,
            onBack = { navController.popBackStack() },
        )
        DepuracionPhase.ERROR -> DepuracionErrorContent(
            state = state,
            onRetry = viewModel::onRetry,
            onCancel = { navController.popBackStack() },
        )
    }

    if (state.showDatePicker) {
        DepuracionDatePickerDialog(
            initialDateMs = state.cutoffDateMs,
            onConfirm = viewModel::onCutoffChanged,
            onDismiss = viewModel::onDismissDatePicker,
        )
    }

    if (state.showConfirmDialog) {
        DepuracionConfirmDialog(
            state = state,
            onInputChanged = viewModel::onConfirmInputChanged,
            onConfirm = viewModel::onConfirmDelete,
            onDismiss = viewModel::onCancelConfirm,
        )
    }
}

@Composable
private fun DepuracionConfigContent(
    state: DepuracionUiState,
    onBack: () -> Unit,
    onCutoffClick: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit,
    onExportarClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Warning banner
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ext.redTint)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ext.redText, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = stringResource(R.string.depuracion_warning_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ext.redText,
                    )
                    Text(
                        text = stringResource(R.string.depuracion_warning_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.redText,
                    )
                }
            }

            // Phase steps
            PhaseSteps(activePhase = 0)

            Spacer(Modifier.height(8.dp))

            // Scope section
            SectionLabel(stringResource(R.string.depuracion_section_que))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                    .background(ext.surface2),
            ) {
                ConfigRow(
                    label = stringResource(R.string.depuracion_registros_label),
                    value = stringResource(R.string.depuracion_registros_value),
                    onClick = null,
                )
                HorizontalDivider(color = ext.border, modifier = Modifier.padding(start = 16.dp))
                ConfigRow(
                    label = stringResource(R.string.depuracion_cutoff_label),
                    value = formatDateMs(state.cutoffDateMs),
                    onClick = onCutoffClick,
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ext.text3, modifier = Modifier.size(16.dp))
                    },
                )
            }

            Spacer(Modifier.height(14.dp))

            // Format section
            SectionLabel(stringResource(R.string.depuracion_section_formato))
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                    .background(ext.surface2)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FormatChip(
                    label = "XLSX",
                    selected = state.format == ExportFormat.XLSX,
                    onClick = { onFormatSelected(ExportFormat.XLSX) },
                    modifier = Modifier.weight(1f),
                )
                FormatChip(
                    label = "CSV",
                    selected = state.format == ExportFormat.CSV,
                    onClick = { onFormatSelected(ExportFormat.CSV) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            // Record count pill
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                when {
                    state.isCountLoading -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    state.recordCount != null -> {
                        val countColor = if (state.recordCount > 0) ext.redText else ext.greenText
                        val countBg = if (state.recordCount > 0) ext.redTint else ext.greenTint
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(countBg)
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.depuracion_count_pill, state.recordCount),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = countColor,
                            )
                        }
                    }
                    else -> {}
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))

            // CTA button
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (state.recordCount != null && state.recordCount > 0) ext.redText
                        else ext.border2
                    )
                    .clickable(enabled = state.recordCount != null && state.recordCount > 0) { onExportarClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.depuracion_cta),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.recordCount != null && state.recordCount > 0)
                        Color.White else ext.text3,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DepuracionProgressContent(
    state: DepuracionUiState,
    isExporting: Boolean,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    val progressColor = if (isExporting) accent else ext.redText
    val iconTint = if (isExporting) ext.greenTint else ext.redTint
    val labelRes = if (isExporting) R.string.depuracion_phase1_label else R.string.depuracion_phase2_label

    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(300),
        label = "progress",
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PhaseSteps(activePhase = if (isExporting) 1 else 2)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(iconTint),
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = progressColor, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = progressColor,
                trackColor = ext.surface2,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "${state.currentCount} / ${state.totalCount}",
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun DepuracionErrorContent(
    state: DepuracionUiState,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
                onBack = onCancel,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PhaseSteps(activePhase = 1, hasFailed = true)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ext.redTint),
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = ext.redText, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.depuracion_error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.depuracion_error_body),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ext.greenTint)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
            ) {
                Text(
                    text = stringResource(R.string.depuracion_error_safe_pill),
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.greenText,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text4,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, ext.border2, RoundedCornerShape(14.dp))
                        .clickable { onCancel() },
                ) {
                    Text(
                        text = stringResource(R.string.common_cancelar),
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.text2,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ext.redText)
                        .clickable { onRetry() },
                ) {
                    Text(
                        text = stringResource(R.string.depuracion_retry),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DepuracionDoneContent(
    state: DepuracionUiState,
    onBack: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PhaseSteps(activePhase = 3)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ext.greenTint),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.depuracion_done_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.depuracion_done_body, state.deletedCount),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                textAlign = TextAlign.Center,
            )

            // File card
            if (state.exportedFileName != null) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                        .background(ext.surface2)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.exportedFileName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ext.text2,
                        )
                        Text(
                            text = formatFileSize(state.exportedFileSize),
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.text3,
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ext.surface3)
                            .clickable {
                                state.exportedFileUri?.let { uri ->
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            },
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ext.text2, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent)
                    .clickable { onBack() },
            ) {
                Text(
                    text = stringResource(R.string.depuracion_done_cta),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun DepuracionConfirmDialog(
    state: DepuracionUiState,
    onInputChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val isValid = state.confirmInput.trim().uppercase() == "ELIMINAR"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ext.redTint),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ext.redText, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.depuracion_confirm_title, state.recordCount ?: 0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.depuracion_confirm_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text3,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.depuracion_confirm_type_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.text3,
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.confirmInput,
                    onValueChange = onInputChanged,
                    placeholder = { Text("ELIMINAR") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isValid) ext.redText else ext.border2)
                    .clickable(enabled = isValid) { onConfirm() },
            ) {
                Text(
                    text = stringResource(R.string.depuracion_confirm_cta),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isValid) Color.White else ext.text3,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancelar), color = ext.text2)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepuracionDatePickerDialog(
    initialDateMs: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMs)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let(onConfirm)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun PhaseSteps(
    activePhase: Int,
    hasFailed: Boolean = false,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepCircle(number = 1, label = "Exportar", active = activePhase >= 1, done = activePhase > 1, failed = hasFailed && activePhase == 1)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(if (activePhase > 1) accent else ext.border2),
        )
        StepCircle(number = 2, label = "Eliminar", active = activePhase >= 2, done = activePhase > 2, failed = hasFailed && activePhase == 2)
    }
}

@Composable
private fun StepCircle(
    number: Int,
    label: String,
    active: Boolean,
    done: Boolean,
    failed: Boolean,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    val bgColor = when {
        failed -> ext.redText
        done || active -> accent
        else -> ext.surface2
    }
    val fgColor = when {
        failed || done || active -> MaterialTheme.colorScheme.onPrimary
        else -> ext.text3
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.dp, if (!active && !done && !failed) ext.border2 else Color.Transparent, CircleShape),
        ) {
            if (done) {
                Icon(Icons.Default.Check, contentDescription = null, tint = fgColor, modifier = Modifier.size(16.dp))
            } else {
                Text(text = number.toString(), color = fgColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (active || done) MaterialTheme.colorScheme.onBackground else ext.text3, fontSize = 11.sp)
    }
}

@Composable
private fun ConfigRow(
    label: String,
    value: String,
    onClick: (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val ext = MaterialTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = ext.text2, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        if (trailingIcon != null) {
            Spacer(Modifier.width(6.dp))
            trailingIcon()
        } else if (onClick != null) {
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun FormatChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val ext = MaterialTheme.extendedColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent else Color.Transparent)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else ext.text2,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

private fun formatDateMs(ms: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(ms))

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
