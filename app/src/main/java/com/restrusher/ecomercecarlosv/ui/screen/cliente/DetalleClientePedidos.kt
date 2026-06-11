package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun PedidosSection(
    pedidos: List<Pedido>,
    onPedidoClick: (String) -> Unit,
    onNuevoPedidoClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.extendedColors
    Text(
        text = stringResource(R.string.detalle_cliente_pedidos_section).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = ext.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
    )
    if (pedidos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Receipt,
            title = stringResource(R.string.detalle_cliente_pedidos_empty_title),
            subtitle = stringResource(R.string.detalle_cliente_pedidos_empty_subtitle),
            hint = stringResource(R.string.detalle_cliente_pedidos_empty_hint),
            compact = true,
            modifier = Modifier.height(240.dp),
            onActionClick = onNuevoPedidoClick,
        )
    } else {
        pedidos.forEachIndexed { index, pedido ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 75.dp),
                    color = ext.border,
                )
            }
            PedidoRow(pedido = pedido, onClick = { onPedidoClick(pedido.id) })
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewPedidos = listOf(
    Pedido(id = "o1", clienteId = "c1", status = PedidoStatus.PENDING, total = 47.50, paid = 0.0, createdAt = 1748822400000L, itemCount = 3),
    Pedido(id = "o2", clienteId = "c1", status = PedidoStatus.PARTIAL, total = 112.00, paid = 50.0, createdAt = 1748390400000L, itemCount = 5),
    Pedido(id = "o3", clienteId = "c1", status = PedidoStatus.PAID, total = 24.00, paid = 24.0, createdAt = 1747872000000L, itemCount = 2),
    Pedido(id = "o4", clienteId = "c1", status = PedidoStatus.PENDING, total = 60.00, paid = 0.0, createdAt = 1747353600000L, isSaldoExtra = true, notes = "Envases retornables"),
)

@Preview(name = "PedidosSection — filled dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidosSectionFilledPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column {
                PedidosSection(pedidos = previewPedidos, onPedidoClick = {})
            }
        }
    }
}

@Preview(name = "PedidosSection — filled light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PedidosSectionFilledLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        Surface {
            Column {
                PedidosSection(pedidos = previewPedidos, onPedidoClick = {})
            }
        }
    }
}

@Preview(name = "PedidosSection — empty dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidosSectionEmptyPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column {
                PedidosSection(pedidos = emptyList(), onPedidoClick = {}, onNuevoPedidoClick = {})
            }
        }
    }
}
