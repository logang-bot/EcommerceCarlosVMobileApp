package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun LineItemsSection(detalles: List<DetallePedido>) {
    detalles.forEach { detalle ->
        LineItemRow(detalle = detalle)
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
fun LineItemRow(modifier: Modifier = Modifier, detalle: DetallePedido) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detalle.productName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.detalle_pedido_qty_precio, detalle.quantity, "%.2f".format(detalle.unitPrice)),
                        fontSize = 12.5.sp,
                        color = ext.text2,
                    )
                    if (detalle.isPriceModified) {
                        Text(
                            text = "Bs. ${"%.2f".format(detalle.catalogPrice)}",
                            fontSize = 11.sp,
                            color = ext.text3,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                }
            }
            Text(
                text = "Bs. ${"%.2f".format(detalle.subtotal)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        if (detalle.isPriceModified) {
            PriceModifiedHint()
        }
        detalle.notes?.let { note ->
            Text(
                text = note,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = ext.text3,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun PriceModifiedHint() {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .background(ext.amberTint, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.pedidos_precio_modificado),
            fontSize = 11.sp,
            color = ext.amberText,
            fontWeight = FontWeight.Medium,
        )
    }
}
