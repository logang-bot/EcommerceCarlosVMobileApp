package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PayChip
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidoRow(
    modifier: Modifier = Modifier,
    pedido: Pedido,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val dateStr = remember(pedido.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date(pedido.createdAt))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (pedido.isSaldoExtra) Color(0x0BE7B23E) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PedidoIconTile(isSaldoExtra = pedido.isSaldoExtra)

        if (pedido.isSaldoExtra) {
            SaldoExtraRowContent(
                dateStr = dateStr,
                notes = pedido.notes,
                total = pedido.total,
                status = pedido.status,
                modifier = Modifier.weight(1f),
            )
        } else {
            OrderRowContent(
                dateStr = dateStr,
                itemCount = pedido.itemCount,
                pedido = pedido,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PedidoIconTile(isSaldoExtra: Boolean) {
    val ext = MaterialTheme.extendedColors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSaldoExtra) ext.amberTint else ext.surface3)
            .then(
                if (!isSaldoExtra) Modifier.border(1.dp, ext.border, RoundedCornerShape(12.dp))
                else Modifier,
            ),
    ) {
        Icon(
            imageVector = if (isSaldoExtra) Icons.Default.Tag else Icons.Default.Receipt,
            contentDescription = null,
            tint = if (isSaldoExtra) ext.amberText else ext.text2,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SaldoExtraRowContent(
    dateStr: String,
    notes: String?,
    total: Double,
    status: PedidoStatus,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = stringResource(R.string.pedidos_row_saldo_extra),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                ManualBadge()
            }
            Text(
                text = buildString {
                    append(dateStr)
                    if (!notes.isNullOrBlank()) {
                        append(" · ")
                        append(notes)
                    }
                },
                fontSize = 12.sp,
                color = ext.text3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Text(
                text = "Bs. ${"%.2f".format(total)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
            )
            Spacer(Modifier.height(5.dp))
            PayChip(status = status)
        }
    }
}

@Composable
private fun OrderRowContent(
    dateStr: String,
    itemCount: Int,
    pedido: Pedido,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val isPartial = pedido.status == PedidoStatus.PARTIAL
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateStr,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.pedidos_row_n_productos, itemCount),
                fontSize = 12.5.sp,
                color = ext.text3,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            if (isPartial) {
                PartialAmountDisplay(total = pedido.total, pending = pedido.pending)
            } else {
                Text(
                    text = "Bs. ${"%.2f".format(pedido.total)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                )
            }
            Spacer(Modifier.height(5.dp))
            PayChip(status = pedido.status)
        }
    }
}

@Composable
private fun PartialAmountDisplay(total: Double, pending: Double) {
    val ext = MaterialTheme.extendedColors
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Bs. ${"%.2f".format(total)}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = ext.text3,
            textDecoration = TextDecoration.LineThrough,
        )
        Text(
            text = "Bs. ${"%.2f".format(pending)}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp,
            color = ext.blueText,
        )
    }
}

@Composable
private fun ManualBadge() {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(ext.amberTint)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(R.string.pedidos_row_saldo_extra_badge).uppercase(),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp,
            color = ext.amberText,
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private fun makeOrderPedido(status: PedidoStatus, paid: Double = 0.0, total: Double = 112.00) = Pedido(
    id = "p1", clienteId = "c1", status = status,
    total = total, paid = paid,
    createdAt = 1748476800000L,
    itemCount = 5,
)

private val saldoPedido = Pedido(
    id = "p2", clienteId = "c1", status = PedidoStatus.PENDING,
    total = 60.00, paid = 0.0,
    createdAt = 1748476800000L,
    isSaldoExtra = true,
    notes = "Envases retornables",
    itemCount = 0,
)

@Preview(name = "Pending — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoRowPendingPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PENDING), onClick = {}) }
    }
}

@Preview(name = "Partial — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoRowPartialPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PARTIAL, paid = 50.0), onClick = {}) }
    }
}

@Preview(name = "Paid — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoRowPaidPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PAID, paid = 112.0), onClick = {}) }
    }
}

@Preview(name = "Saldo extra — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoRowSaldoPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoRow(pedido = saldoPedido, onClick = {}) }
    }
}

@Preview(name = "Partial — light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PedidoRowPartialLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PARTIAL, paid = 50.0), onClick = {}) }
    }
}
