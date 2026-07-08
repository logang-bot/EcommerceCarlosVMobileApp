package com.restrusher.ecomercecarlosv.ui.screen.reporte.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ReporteStatCard(
    label: String,
    value: String,
    valueColor: Color,
    iconBgColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
        ) {
            Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.5).sp,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = ext.text2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FacturadoHeroCard(
    total: Double,
    pedidosCount: Int,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ext.accentTint),
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
            }
            Text(
                text = stringResource(R.string.reporte_facturado_hoy),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ext.text2,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.reporte_pedidos_count, pedidosCount),
                fontSize = 12.sp,
                color = ext.text3,
            )
        }
        Text(
            text = "Bs. ${"%.2f".format(total)}",
            fontSize = 34.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
fun PagadoPorPagarRow(
    pagado: Double,
    porPagar: Double,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MoneyMiniCard(
            label = stringResource(R.string.reporte_pagado),
            value = pagado,
            icon = Icons.Default.Check,
            tint = ext.greenTint,
            textColor = ext.greenText,
            modifier = Modifier.weight(1f),
        )
        MoneyMiniCard(
            label = stringResource(R.string.reporte_por_pagar),
            value = porPagar,
            icon = Icons.Default.AttachMoney,
            tint = ext.amberTint,
            textColor = ext.amberText,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MoneyMiniCard(
    label: String,
    value: Double,
    icon: ImageVector,
    tint: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint)
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(15.dp))
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
        Text(
            text = "Bs. ${"%.2f".format(value)}",
            fontSize = 21.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.5).sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 9.dp),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReporteStatCardPreview() {
    val ext = MaterialTheme.extendedColors
    EcomerceCarlosVTheme {
        ReporteStatCard(
            label = "Pedidos creados",
            value = "5",
            valueColor = MaterialTheme.colorScheme.primary,
            iconBgColor = ext.accentTint,
            icon = Icons.Default.ShoppingCart,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FacturadoHeroCardPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FacturadoHeroCard(total = 855.50, pedidosCount = 14, modifier = Modifier.padding(horizontal = 20.dp))
            PagadoPorPagarRow(pagado = 617.50, porPagar = 238.0, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}
