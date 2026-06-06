package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sell
import android.content.res.Configuration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
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
    val ext = MaterialTheme.extendedColors
    detalles.forEachIndexed { index, detalle ->
        if (index > 0) {
            HorizontalDivider(modifier = Modifier.padding(start = 70.dp), color = ext.border)
        }
        LineItemRow(detalle = detalle)
    }
}

@Composable
fun LineItemRow(modifier: Modifier = Modifier, detalle: DetallePedido) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(ext.surface3, RoundedCornerShape(10.dp))
                .border(1.dp, ext.border, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Sell, contentDescription = null, tint = ext.text3, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = detalle.productName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                )
                Text(
                    text = "Bs. ${"%.2f".format(detalle.subtotal)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = "${detalle.quantity} × Bs. ${"%.2f".format(detalle.unitPrice)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = ext.text2,
                )
                if (detalle.isPriceModified) {
                    Box(modifier = Modifier.size(5.dp).background(ext.amberText, CircleShape))
                    Text(
                        text = "Bs. ${"%.2f".format(detalle.catalogPrice)}",
                        fontSize = 12.sp,
                        color = ext.amberText,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
            if (detalle.isPriceModified) {
                PriceModifiedHint()
            }
            detalle.notes?.let { note ->
                Text(
                    text = "\"$note\"",
                    fontSize = 12.5.sp,
                    fontStyle = FontStyle.Italic,
                    color = ext.text3,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val previewDetalleSimple = DetallePedido(
    id = "d1", pedidoId = "1", productoId = "p1",
    productName = "Harina PAN 1kg", quantity = 12, unitPrice = 2.50, catalogPrice = 2.50,
)
private val previewDetallePriceModified = DetallePedido(
    id = "d2", pedidoId = "1", productoId = "p2",
    productName = "Café Madrid 250g", quantity = 4, unitPrice = 3.00, catalogPrice = 5.20,
    notes = "Descuento acordado — cliente frecuente",
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LineItemRowPreview() {
    EcomerceCarlosVTheme {
        LineItemsSection(detalles = listOf(previewDetalleSimple, previewDetallePriceModified))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LineItemRowDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LineItemsSection(detalles = listOf(previewDetalleSimple, previewDetallePriceModified))
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
