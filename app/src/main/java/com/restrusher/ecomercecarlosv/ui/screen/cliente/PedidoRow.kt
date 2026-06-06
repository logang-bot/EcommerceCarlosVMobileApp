package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (pedido.isSaldoExtra) ext.amberTint else ext.surface3),
        ) {
            Icon(
                imageVector = if (pedido.isSaldoExtra) Icons.Default.Tag else Icons.Default.Receipt,
                contentDescription = null,
                tint = if (pedido.isSaldoExtra) ext.amberText else ext.text3,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when {
                    pedido.isSaldoExtra -> stringResource(R.string.pedidos_row_saldo_extra)
                    detalleCount > 0 -> stringResource(R.string.pedidos_row_n_productos, detalleCount)
                    else -> stringResource(R.string.pedidos_row_label)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(dateStr, fontSize = 12.sp, color = ext.text3)
        }
        Text(
            text = "Bs. ${"%.2f".format(pedido.total)}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
        Spacer(Modifier.width(6.dp))
        if (pedido.isSaldoExtra) {
            SaldoExtraBadge()
        } else {
            PayChip(status = pedido.status)
        }
    }
}

@Composable
private fun SaldoExtraBadge() {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ext.amberTint)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = stringResource(R.string.pedidos_row_saldo_extra_badge),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ext.amberText,
        )
    }
}
