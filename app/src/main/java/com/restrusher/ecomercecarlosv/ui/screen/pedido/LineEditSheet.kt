package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineEditSheet(
    item: CartItem,
    onSave: (CartItem) -> Unit,
    onDismiss: () -> Unit,
) {
    var quantity by remember { mutableStateOf(item.quantity) }
    var priceText by remember { mutableStateOf("%.2f".format(item.unitPrice)) }
    var notes by remember { mutableStateOf(item.notes ?: "") }

    val price = priceText.replace(",", ".").toDoubleOrNull() ?: item.unitPrice
    val priceModified = price != item.catalogPrice

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 24.dp)) {
            ProductHeader(item)
            Spacer(Modifier.height(16.dp))
            QuantityField(quantity = quantity, onDecrement = { if (quantity > 1) quantity-- }, onIncrement = { quantity++ })
            Spacer(Modifier.height(14.dp))
            PriceField(priceText = priceText, onValueChange = { priceText = it }, isModified = priceModified, catalogPrice = item.catalogPrice)
            if (priceModified) {
                Spacer(Modifier.height(8.dp))
                PriceModifiedBanner(catalogPrice = item.catalogPrice, newPrice = price)
            }
            Spacer(Modifier.height(14.dp))
            NotesField(notes = notes, onValueChange = { notes = it })
            Spacer(Modifier.height(14.dp))
            SubtotalRow(subtotal = price * quantity)
            Spacer(Modifier.height(14.dp))
            TextButton(
                onClick = { onSave(item.copy(quantity = quantity, unitPrice = price, notes = notes.ifBlank { null })) },
                modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
            ) {
                Text(stringResource(R.string.common_confirmar), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ProductHeader(item: CartItem) {
    val ext = MaterialTheme.extendedColors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(ext.surface3)) {
            Icon(Icons.Default.Tag, contentDescription = null, tint = ext.text3, modifier = Modifier.size(28.dp))
        }
        Column {
            Text(item.productName, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
            Text(stringResource(R.string.pedidos_precio_catalogo_ref, "%.2f".format(item.catalogPrice)), fontSize = 12.5.sp, color = ext.text3)
        }
    }
}

@Composable
private fun QuantityField(quantity: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Text(stringResource(R.string.pedidos_cantidad_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(15.dp)).background(ext.surface2).border(1.dp, ext.border2, RoundedCornerShape(15.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
        Text("$quantity", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        IconButton(onClick = onIncrement, modifier = Modifier.padding(end = 4.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PriceField(priceText: String, onValueChange: (String) -> Unit, isModified: Boolean, catalogPrice: Double) {
    val ext = MaterialTheme.extendedColors
    val borderColor = if (isModified) ext.amber else ext.border2
    Text(stringResource(R.string.pedidos_precio_unitario_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
    OutlinedTextField(
        value = priceText,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        prefix = { Text("Bs.", fontSize = 14.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
    )
}

@Composable
private fun PriceModifiedBanner(catalogPrice: Double, newPrice: Double) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(ext.amberTint).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Tag, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(16.dp))
        Column {
            Text(stringResource(R.string.pedidos_precio_modificado), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ext.amberText)
            Text(
                text = stringResource(R.string.pedidos_precio_modificado_desc, "%.2f".format(catalogPrice), "%.2f".format(newPrice)),
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun NotesField(notes: String, onValueChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(modifier = Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.pedidos_nota_label), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(stringResource(R.string.pedidos_nota_optional), fontSize = 13.sp, color = ext.text3)
    }
    OutlinedTextField(
        value = notes,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 4,
        placeholder = { Text(stringResource(R.string.pedidos_nota_placeholder), color = ext.text3) },
        shape = RoundedCornerShape(14.dp),
    )
}

@Composable
private fun SubtotalRow(subtotal: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.pedidos_subtotal_linea), fontSize = 13.sp, color = MaterialTheme.extendedColors.text2)
        Text("Bs. ${"%.2f".format(subtotal)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = (-0.5).sp)
    }
}
