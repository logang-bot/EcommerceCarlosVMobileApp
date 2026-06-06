package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TotalBlock(pedido: Pedido) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.detalle_pedido_total_label), fontSize = 14.sp, color = ext.text2)
            Text("Bs. ${"%.2f".format(pedido.total)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = ext.text2)
        }
        if (pedido.paid > 0) {
            Spacer(Modifier.height(9.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.detalle_pedido_pagado_label), fontSize = 14.sp, color = ext.greenText)
                Text("− Bs. ${"%.2f".format(pedido.paid)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = ext.greenText)
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = ext.border)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.detalle_pedido_pendiente_label),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            val pendingColor = if (pedido.pending > 0) ext.amberText else ext.text2
            Text(
                text = "Bs. ${"%.2f".format(pedido.pending)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = pendingColor,
            )
        }
    }
}

@Composable
fun PagosSection(pedido: Pedido) {
    val ext = MaterialTheme.extendedColors
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es"))
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
        DetalleSectionLabel(stringResource(R.string.detalle_pedido_pagos_section))
        val date = dateFormatter.format(Date(pedido.paidAt ?: pedido.createdAt))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(ext.greenTint, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(16.dp))
            }
            Text(date, modifier = Modifier.weight(1f), fontSize = 13.5.sp, color = ext.text2)
            Text(
                text = "+ Bs. ${"%.2f".format(pedido.paid)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ext.greenText,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val previewPedidoPartial = Pedido(
    id = "1", clienteId = "c1", status = PedidoStatus.PARTIAL,
    total = 112.00, paid = 50.00, createdAt = 1748390400000L,
)
private val previewPedidoPaid = Pedido(
    id = "2", clienteId = "c1", status = PedidoStatus.PAID,
    total = 112.00, paid = 112.00, createdAt = 1748390400000L, paidAt = 1748736000000L,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun TotalBlockPreview() {
    EcomerceCarlosVTheme { TotalBlock(pedido = previewPedidoPartial) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TotalBlockDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) { TotalBlock(pedido = previewPedidoPartial) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PagosSectionPreview() {
    EcomerceCarlosVTheme { PagosSection(pedido = previewPedidoPaid) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PagosSectionDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) { PagosSection(pedido = previewPedidoPaid) }
}
