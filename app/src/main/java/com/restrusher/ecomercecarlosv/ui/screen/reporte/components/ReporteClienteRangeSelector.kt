package com.restrusher.ecomercecarlosv.ui.screen.reporte.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClientePreset
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClienteUiState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IntroText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.reporte_pedidos_intro),
        fontSize = 13.5.sp,
        color = MaterialTheme.extendedColors.text2,
        lineHeight = 20.sp,
        modifier = modifier,
    )
}

@Composable
fun RangeSectionLabel(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.reporte_rango_rapido).uppercase(),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetChipsRow(
    preset: ReporteClientePreset,
    onSelect: (ReporteClientePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReporteClientePresetChip(
            label = stringResource(R.string.reporte_preset_hoy),
            selected = preset == ReporteClientePreset.HOY,
            onClick = { onSelect(ReporteClientePreset.HOY) },
        )
        ReporteClientePresetChip(
            label = stringResource(R.string.reporte_preset_esta_semana),
            selected = preset == ReporteClientePreset.SEMANA,
            onClick = { onSelect(ReporteClientePreset.SEMANA) },
        )
        ReporteClientePresetChip(
            label = stringResource(R.string.reporte_preset_mes),
            selected = preset == ReporteClientePreset.MES,
            onClick = { onSelect(ReporteClientePreset.MES) },
        )
        ReporteClientePresetChip(
            label = stringResource(R.string.reporte_preset_personalizado),
            selected = preset == ReporteClientePreset.PERSONALIZADO,
            onClick = { onSelect(ReporteClientePreset.PERSONALIZADO) },
        )
    }
}

@Composable
fun ReporteClientePresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(11.dp)
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else ext.surface2
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(bgColor)
            .then(if (!selected) Modifier.border(1.dp, ext.border2, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}

@Composable
fun ResolvedDateBar(
    state: ReporteClienteUiState,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(12.dp)
    val text = remember(state.preset, state.fromMs, state.toMs) {
        if (state.fromMs > 0L) formatResolvedBarText(state) else ""
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .background(ext.surface2)
            .border(1.dp, ext.border, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = null,
            tint = ext.text3,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = ext.text2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CustomDateFields(
    fromMs: Long?,
    toMs: Long?,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val df = remember { SimpleDateFormat("d MMM yyyy", Locale("es")) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DateFieldButton(
            label = stringResource(R.string.reporte_desde),
            value = fromMs?.let { df.format(Date(it)) },
            onClick = onFromClick,
            modifier = Modifier.weight(1f),
        )
        DateFieldButton(
            label = stringResource(R.string.reporte_hasta),
            value = toMs?.let { df.format(Date(it)) },
            onClick = onToClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun DateFieldButton(
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(13.dp)
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = ext.text2,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(shape)
                .background(ext.surface2)
                .border(1.dp, ext.border2, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value ?: "—",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (value != null) MaterialTheme.colorScheme.onSurface else ext.text3,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ext.text4,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

fun formatResolvedBarText(state: ReporteClienteUiState): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val dfShort = SimpleDateFormat("d MMM", Locale("es"))
    return when (state.preset) {
        ReporteClientePreset.HOY -> "Hoy · ${df.format(Date(state.fromMs))}"
        ReporteClientePreset.SEMANA ->
            "Esta semana · ${dfShort.format(Date(state.fromMs))} – ${df.format(Date(state.toMs))}"
        ReporteClientePreset.MES ->
            "Este mes · ${dfShort.format(Date(state.fromMs))} – ${df.format(Date(state.toMs))}"
        ReporteClientePreset.PERSONALIZADO -> ""
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewState = ReporteClienteUiState(
    preset = ReporteClientePreset.SEMANA,
    fromMs = 1_749_081_600_000L,
    toMs = 1_749_600_000_000L,
    isLoading = false,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PresetChipsRowPreview() {
    EcomerceCarlosVTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PresetChipsRow(preset = ReporteClientePreset.SEMANA, onSelect = {})
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ResolvedDateBarPreview() {
    EcomerceCarlosVTheme {
        ResolvedDateBar(state = previewState, modifier = Modifier.padding(16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CustomDateFieldsDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        CustomDateFields(
            fromMs = 1_749_081_600_000L,
            toMs = null,
            onFromClick = {},
            onToClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
