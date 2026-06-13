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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.restrusher.ecomercecarlosv.ui.screen.reporte.MovimientoItem
import com.restrusher.ecomercecarlosv.ui.screen.reporte.MovimientoType
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteUiState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CobradoHeroCard(state: ReporteUiState) {
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
                text = stringResource(R.string.reporte_cobrado_hoy).uppercase(),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
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
                color = ext.text2,
            )
        }
    }
}

@Composable
fun DiarioStatCards(state: ReporteUiState) {
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
            iconBgColor = ext.accentTint,
            icon = Icons.Default.ShoppingCart,
            modifier = Modifier.weight(1f),
        )
        ReporteStatCard(
            label = stringResource(R.string.reporte_pendiente_dia),
            value = "Bs. ${"%.2f".format(state.pendienteDelDia)}",
            valueColor = ext.amberText,
            iconBgColor = ext.amberTint,
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.weight(1f),
        )
    }
}

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
            contentAlignment = androidx.compose.ui.Alignment.Center,
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
fun MovimientosSectionHeader() {
    Text(
        text = stringResource(R.string.reporte_movimientos).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
    )
}

@Composable
fun EmptyMovimientos() {
    Text(
        text = stringResource(R.string.reporte_sin_movimientos),
        fontSize = 13.sp,
        color = MaterialTheme.extendedColors.text3,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    )
}

@Composable
fun MovimientoRow(item: MovimientoItem) {
    val ext = MaterialTheme.extendedColors
    val dotColor = if (item.type == MovimientoType.COBRO) ext.green else MaterialTheme.colorScheme.primary
    val timeStr = remember(item.timestamp) {
        SimpleDateFormat("HH:mm", Locale("es")).format(Date(item.timestamp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
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
                color = ext.text3,
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

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewState = ReporteUiState(
    cobradoTotal = 1_245.50,
    cobroCount = 8,
    pedidosCreadosCount = 5,
    pendienteDelDia = 320.0,
    isLoading = false,
)

private val previewMovimiento = MovimientoItem(
    pedidoId = "1",
    clienteName = "Ana Rodríguez",
    mercadoName = "Mercado Central",
    type = MovimientoType.COBRO,
    amount = 150.0,
    timestamp = System.currentTimeMillis(),
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun CobradoHeroCardPreview() {
    EcomerceCarlosVTheme {
        CobradoHeroCard(state = previewState)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DiarioStatCardsDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DiarioStatCards(state = previewState)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun MovimientoRowPreview() {
    EcomerceCarlosVTheme {
        Column {
            MovimientoRow(item = previewMovimiento)
            MovimientoRow(
                item = previewMovimiento.copy(
                    clienteName = "Carlos Mamani",
                    mercadoName = "Mercado Sur",
                    type = MovimientoType.PEDIDO,
                    amount = 80.0,
                ),
            )
        }
    }
}
