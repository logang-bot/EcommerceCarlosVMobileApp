package com.restrusher.ecomercecarlosv.ui.screen.reporte.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteMode
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ReporteModeToggle(mode: ReporteMode, onSetMode: (ReporteMode) -> Unit) {
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
fun ModeButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.extendedColors.text2,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReporteModeToggleDiarioPreview() {
    EcomerceCarlosVTheme {
        ReporteModeToggle(mode = ReporteMode.DIARIO, onSetMode = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ReporteModeToggleDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ReporteModeToggle(mode = ReporteMode.POR_CLIENTE, onSetMode = {})
    }
}
