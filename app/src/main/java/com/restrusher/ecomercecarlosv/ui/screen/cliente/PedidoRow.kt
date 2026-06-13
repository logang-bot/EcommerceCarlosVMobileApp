package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.restrusher.ecomercecarlosv.domain.model.PedidoLineItem
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
    val dateStr = remember(pedido.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date(pedido.createdAt))
    }
    val canExpand = !pedido.isSaldoExtra && pedido.lines.isNotEmpty()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (pedido.isSaldoExtra) Color(0x0BE7B23E) else Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            if (canExpand) {
                CaretButton(expanded = expanded, onClick = { expanded = !expanded })
            } else {
                PedidoIconTile(isSaldoExtra = pedido.isSaldoExtra)
            }

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

        AnimatedVisibility(
            visible = canExpand && expanded,
            enter = expandVertically() + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically() + fadeOut(animationSpec = tween(180)),
        ) {
            PedidoLinesPanel(lines = pedido.lines)
        }
    }
}

@Composable
private fun CaretButton(expanded: Boolean, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(180),
        label = "caret",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (expanded) ext.accentSoft else ext.surface3)
            .border(
                width = 1.dp,
                color = if (expanded) MaterialTheme.colorScheme.primary else ext.border,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (expanded) MaterialTheme.colorScheme.primary else ext.text2,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation),
        )
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
            imageVector = Icons.Default.Tag,
            contentDescription = null,
            tint = ext.amberText,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PedidoLinesPanel(lines: List<PedidoLineItem>) {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .padding(bottom = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, ext.border, RoundedCornerShape(12.dp)),
    ) {
        Column {
            lines.forEachIndexed { index, line ->
                if (index > 0) HorizontalDivider(color = ext.border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = line.productName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "×${line.quantity}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ext.text2,
                    )
                }
            }
        }
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

private val sampleLines = listOf(
    PedidoLineItem("Harina PAN 1kg", 8),
    PedidoLineItem("Azúcar Montalbán 1kg", 5),
    PedidoLineItem("Pasta Capri 500g", 6),
)

private fun makeOrderPedido(
    status: PedidoStatus,
    paid: Double = 0.0,
    total: Double = 112.00,
    lines: List<PedidoLineItem> = sampleLines,
) = Pedido(
    id = "p1", clienteId = "c1", status = status,
    total = total, paid = paid,
    createdAt = 1748476800000L,
    itemCount = lines.size,
    lines = lines,
)

private val saldoPedido = Pedido(
    id = "p2", clienteId = "c1", status = PedidoStatus.PENDING,
    total = 60.00, paid = 0.0,
    createdAt = 1748476800000L,
    isSaldoExtra = true,
    notes = "Envases retornables",
    itemCount = 0,
)

@Preview(name = "Pending collapsed — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoRowPendingPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PENDING), onClick = {}) }
    }
}

@Preview(name = "Partial collapsed — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoRowPartialPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PARTIAL, paid = 50.0), onClick = {}) }
    }
}

@Preview(name = "Paid collapsed — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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

@Preview(name = "Partial collapsed — light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PedidoRowPartialLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        Surface { PedidoRow(pedido = makeOrderPedido(PedidoStatus.PARTIAL, paid = 50.0), onClick = {}) }
    }
}

@Preview(name = "Lines panel — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidoLinesPanelPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { PedidoLinesPanel(lines = sampleLines) }
    }
}
