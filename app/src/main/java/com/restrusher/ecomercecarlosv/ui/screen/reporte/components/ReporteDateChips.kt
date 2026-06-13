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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ClientePreset
import com.restrusher.ecomercecarlosv.ui.screen.reporte.DiarioPreset
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiarioDateChips(preset: DiarioPreset, onSelect: (DiarioPreset) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClienteDateChips(preset: ClientePreset, onSelect: (ClientePreset) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
fun DateChip(label: String, selected: Boolean, onClick: () -> Unit) {
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

@Composable
fun ReporteResolvedDateBar(text: String, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .background(ext.surface2)
            .border(1.dp, ext.border, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ext.text3, modifier = Modifier.size(16.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ext.text2)
    }
}

fun formatDiarioBarText(preset: DiarioPreset, fromMs: Long, toMs: Long): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val dfShort = SimpleDateFormat("d MMM", Locale("es"))
    return when (preset) {
        DiarioPreset.HOY -> "Hoy · ${df.format(Date(fromMs))}"
        DiarioPreset.AYER -> "Ayer · ${df.format(Date(fromMs))}"
        DiarioPreset.SEMANA -> "Esta semana · ${dfShort.format(Date(fromMs))} – ${df.format(Date(toMs))}"
        DiarioPreset.PERSONALIZADO -> ""
    }
}

fun formatClienteBarText(preset: ClientePreset, fromMs: Long, toMs: Long): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val dfShort = SimpleDateFormat("d MMM", Locale("es"))
    return when (preset) {
        ClientePreset.MES -> "Este mes · ${dfShort.format(Date(fromMs))} – ${df.format(Date(toMs))}"
        ClientePreset.TRIMESTRE -> "Trimestre · ${dfShort.format(Date(fromMs))} – ${df.format(Date(toMs))}"
        ClientePreset.ANIO -> "Año · ${dfShort.format(Date(fromMs))} – ${df.format(Date(toMs))}"
        ClientePreset.PERSONALIZADO -> ""
    }
}

@Composable
fun CustomDateRow(
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
fun DateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = ext.text3,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (value == "—") ext.text4 else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DiarioDateChipsPreview() {
    EcomerceCarlosVTheme {
        DiarioDateChips(preset = DiarioPreset.HOY, onSelect = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ClienteDateChipsDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ClienteDateChips(preset = ClientePreset.MES, onSelect = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun CustomDateRowPreview() {
    EcomerceCarlosVTheme {
        CustomDateRow(
            fromMs = 1_749_686_400_000L,
            toMs = null,
            onFromClick = {},
            onToClick = {},
        )
    }
}
