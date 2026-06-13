package com.restrusher.ecomercecarlosv.ui.screen.reporte.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun SummaryCard(
    count: Int,
    total: Double,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ext.surface2)
            .border(1.dp, ext.border, shape)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reporte_pedidos_en_rango),
                fontSize = 13.sp,
                color = ext.text2,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }
        HorizontalDivider(color = ext.border)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reporte_monto_total),
                fontSize = 13.sp,
                color = ext.text2,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Bs. ${"%.2f".format(total)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
fun WarningBanner(modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ext.amberTint)
            .border(1.dp, ext.amber.copy(alpha = 0.22f), shape)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = ext.amberText,
            modifier = Modifier.size(19.dp).padding(top = 1.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.reporte_rango_amplio),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ext.amberText,
            )
            Text(
                text = stringResource(R.string.reporte_rango_amplio_desc),
                fontSize = 12.sp,
                color = ext.amberText.copy(alpha = 0.8f),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun SummaryCardPreview() {
    EcomerceCarlosVTheme {
        SummaryCard(count = 12, total = 1_245.50, modifier = Modifier.padding(16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryCardDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        SummaryCard(count = 0, total = 0.0, modifier = Modifier.padding(16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun WarningBannerPreview() {
    EcomerceCarlosVTheme {
        WarningBanner(modifier = Modifier.padding(16.dp))
    }
}
