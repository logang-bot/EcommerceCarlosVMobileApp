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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PayChip
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClientePreset
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClienteUiState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_PREVIEW = 5

@Composable
fun PreviewListSection(
    state: ReporteClienteUiState,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val preview = state.pedidosInRange.take(MAX_PREVIEW)
    val extraCount = state.pedidosCount - preview.size

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val sectionTitle = if (state.preset == ReporteClientePreset.HOY) {
                stringResource(R.string.reporte_pedidos_de_hoy).uppercase()
            } else {
                stringResource(R.string.reporte_pedidos_en_reporte).uppercase()
            }
            Text(
                text = sectionTitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ext.text3,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            if (state.preset != ReporteClientePreset.HOY && state.pedidosCount > 0) {
                Text(
                    text = stringResource(
                        R.string.reporte_de_n,
                        preview.size.coerceAtMost(state.pedidosCount),
                        state.pedidosCount,
                    ),
                    fontSize = 11.5.sp,
                    color = ext.text3,
                )
            }
        }

        if (preview.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.reporte_pedidos_vacio),
                    fontSize = 13.sp,
                    color = ext.text3,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border, RoundedCornerShape(16.dp)),
            ) {
                preview.forEachIndexed { index, pedido ->
                    if (index > 0) HorizontalDivider(color = ext.border)
                    ReportePedidoPreviewRow(pedido = pedido)
                }
                if (extraCount > 0) {
                    HorizontalDivider(color = ext.border)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "· ${stringResource(R.string.reporte_y_mas_pdf, extraCount)}",
                            fontSize = 12.5.sp,
                            color = ext.text3,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportePedidoPreviewRow(pedido: Pedido, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val dateStr = remember(pedido.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date(pedido.createdAt))
    }
    val isPartial = pedido.status == PedidoStatus.PARTIAL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = if (pedido.isSaldoExtra) stringResource(R.string.pedidos_row_saldo_extra) else dateStr,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                when {
                    !pedido.isSaldoExtra -> Text(
                        text = stringResource(R.string.pedidos_row_n_productos, pedido.itemCount),
                        fontSize = 12.sp,
                        color = ext.text3,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                    !pedido.notes.isNullOrBlank() -> Text(
                        text = "$dateStr · ${pedido.notes}",
                        fontSize = 12.sp,
                        color = ext.text3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isPartial) "Bs. ${"%.2f".format(pedido.pending)}"
                    else "Bs. ${"%.2f".format(pedido.total)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isPartial) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = if (isPartial) ext.amberText else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                PayChip(status = pedido.status)
            }
        }

        if (!pedido.isSaldoExtra && pedido.lines.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = pedido.lines.joinToString(" · ") { "×${it.quantity} ${it.productName}" },
                fontSize = 11.5.sp,
                color = ext.text4,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun GenerarPdfBar(
    enabled: Boolean,
    onGenerarPdf: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = ext.border, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .navigationBarsPadding(),
    ) {
        Button(
            onClick = onGenerarPdf,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.reporte_generar_pdf),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewPedidos = listOf(
    Pedido("1", "c1", PedidoStatus.PARTIAL, 120.0, 50.0, null, System.currentTimeMillis(), null, false, 3, emptyList()),
    Pedido("2", "c1", PedidoStatus.PAID, 80.0, 80.0, null, System.currentTimeMillis() - 86_400_000L, System.currentTimeMillis(), false, 2, emptyList()),
    Pedido("3", "c1", PedidoStatus.PENDING, 65.0, 0.0, "Nota de prueba", System.currentTimeMillis() - 172_800_000L, null, true, 0, emptyList()),
)

private val previewState = ReporteClienteUiState(
    preset = ReporteClientePreset.SEMANA,
    pedidosInRange = previewPedidos,
    pedidosCount = previewPedidos.size,
    montoTotal = previewPedidos.sumOf { it.total },
    isLoading = false,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReportePedidoPreviewRowPreview() {
    EcomerceCarlosVTheme {
        Column {
            ReportePedidoPreviewRow(pedido = previewPedidos[0])
            ReportePedidoPreviewRow(pedido = previewPedidos[1])
            ReportePedidoPreviewRow(pedido = previewPedidos[2])
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PreviewListSectionPreview() {
    EcomerceCarlosVTheme {
        PreviewListSection(state = previewState, modifier = Modifier.padding(16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun GenerarPdfBarDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        GenerarPdfBar(enabled = true, onGenerarPdf = {})
    }
}
