package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun PendingPedidosList(modifier: Modifier = Modifier, pedidos: List<Pedido>) {
    val ext = MaterialTheme.extendedColors
    if (pedidos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.agregar_lista_negra_pedidos_vacio),
                style = MaterialTheme.typography.bodyMedium,
                color = ext.text3,
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border, RoundedCornerShape(14.dp)),
        ) {
            pedidos.forEachIndexed { index, pedido ->
                if (index > 0) HorizontalDivider(color = ext.border)
                PendingPedidoRow(pedido = pedido)
            }
        }
    }
}

@Composable
private fun PendingPedidoRow(pedido: Pedido) {
    val ext = MaterialTheme.extendedColors
    val dateStr = remember(pedido.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date(pedido.createdAt))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = if (pedido.isSaldoExtra) {
                    stringResource(R.string.pedidos_row_saldo_extra)
                } else {
                    stringResource(R.string.pedidos_row_label)
                },
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = dateStr,
                fontSize = 11.5.sp,
                color = ext.text3,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        Text(
            text = "Bs. ${"%.2f".format(pedido.pending)}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

private val previewPedidos = listOf(
    Pedido(id = "o1", clienteId = "c1", status = PedidoStatus.PENDING, total = 47.50, paid = 0.0, createdAt = 1748822400000L),
    Pedido(id = "o2", clienteId = "c1", status = PedidoStatus.PARTIAL, total = 112.00, paid = 50.0, createdAt = 1748390400000L),
    Pedido(id = "o3", clienteId = "c1", status = PedidoStatus.PENDING, total = 60.00, paid = 0.0, createdAt = 1747958400000L, isSaldoExtra = true),
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PendingPedidosListPreview() {
    EcomerceCarlosVTheme {
        PendingPedidosList(
            modifier = Modifier.padding(16.dp),
            pedidos = previewPedidos,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PendingPedidosListDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        PendingPedidosList(
            modifier = Modifier.padding(16.dp),
            pedidos = previewPedidos,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PendingPedidosListEmptyPreview() {
    EcomerceCarlosVTheme {
        PendingPedidosList(
            modifier = Modifier.padding(16.dp),
            pedidos = emptyList(),
        )
    }
}
