package com.restrusher.ecomercecarlosv.ui.screen.cliente

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.ui.common.PayChip
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidoRow(
    modifier: Modifier = Modifier,
    pedido: Pedido,
    detalleCount: Int = 0,
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (pedido.isSaldoExtra) ext.amberTint else ext.surface3)
                .then(
                    if (!pedido.isSaldoExtra) Modifier.border(1.dp, ext.border, RoundedCornerShape(12.dp))
                    else Modifier
                ),
        ) {
            Icon(
                imageVector = if (pedido.isSaldoExtra) Icons.Default.Tag else Icons.Default.Receipt,
                contentDescription = null,
                tint = if (pedido.isSaldoExtra) ext.amberText else ext.text2,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = when {
                        pedido.isSaldoExtra -> stringResource(R.string.pedidos_row_saldo_extra)
                        detalleCount > 0 -> stringResource(R.string.pedidos_row_n_productos, detalleCount)
                        else -> stringResource(R.string.pedidos_row_label)
                    },
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (pedido.isSaldoExtra) {
                    ManualBadge()
                }
            }
            Text(
                text = buildString {
                    append(dateStr)
                    if (pedido.isSaldoExtra && !pedido.notes.isNullOrBlank()) {
                        append(" · ")
                        append(pedido.notes)
                    }
                },
                fontSize = 12.sp,
                color = ext.text3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Bs. ${"%.2f".format(pedido.total)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
            )
            Spacer(Modifier.height(5.dp))
            PayChip(status = pedido.status)
        }
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
