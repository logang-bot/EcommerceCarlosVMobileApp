package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DepuracionConfigContent(
    state: DepuracionUiState,
    onBack: () -> Unit,
    onCutoffClick: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit,
    onExportarClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionConfigContentPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionConfigContent(
            state = DepuracionUiState(recordCount = 47),
            onBack = {}, onCutoffClick = {}, onFormatSelected = {}, onExportarClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionConfigContentEmptyDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionConfigContent(
            state = DepuracionUiState(recordCount = 0, format = ExportFormat.CSV),
            onBack = {}, onCutoffClick = {}, onFormatSelected = {}, onExportarClick = {},
        )
    }
}
