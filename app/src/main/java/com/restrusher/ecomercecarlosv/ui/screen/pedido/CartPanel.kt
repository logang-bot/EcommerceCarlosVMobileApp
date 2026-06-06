package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import androidx.compose.ui.res.stringResource

@Composable
fun CartPanel(
    modifier: Modifier = Modifier,
    cart: List<CartItem>,
    onRemoveFromCart: (String) -> Unit,
    onEditItem: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val total = cart.sumOf { it.subtotal }
    val hasItems = cart.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(Modifier),
    ) {
        DragHandle()
        CartHeader(itemCount = cart.size)
        if (hasItems) {
            CartItemList(cart = cart, onRemove = onRemoveFromCart, onEdit = onEditItem)
        } else {
            EmptyCartMessage()
        }
        ConfirmButton(total = total, enabled = hasItems, onClick = onConfirm)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.extendedColors.border2),
        )
    }
}

@Composable
private fun CartHeader(itemCount: Int) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ext.text2, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.pedidos_carrito_label), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        if (itemCount > 0) {
            Text("· ${stringResource(R.string.pedidos_carrito_n_productos, itemCount)}", fontSize = 13.sp, color = ext.text3)
        }
    }
}

@Composable
private fun CartItemList(cart: List<CartItem>, onRemove: (String) -> Unit, onEdit: (String) -> Unit) {
    Column(
        modifier = Modifier
            .heightIn(max = 132.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        cart.forEach { item ->
            CartRow(item = item, onRemove = { onRemove(item.productoId) }, onEdit = { onEdit(item.productoId) })
        }
    }
}

@Composable
private fun CartRow(item: CartItem, onRemove: () -> Unit, onEdit: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("×${item.quantity}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("${item.quantity} × Bs. ${"%.2f".format(item.unitPrice)}", fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, color = ext.text3)
        }
        Text("Bs. ${"%.2f".format(item.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = null, tint = ext.text3, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun EmptyCartMessage() {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(R.string.pedidos_carrito_vacio), fontSize = 13.5.sp, color = ext.text3)
    }
}

@Composable
private fun ConfirmButton(total: Double, enabled: Boolean, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    val bgColor = if (enabled) MaterialTheme.colorScheme.primary else ext.surface3
    val textColor = if (enabled) androidx.compose.ui.graphics.Color.White else ext.text3

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.pedidos_confirmar), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textColor)
        if (enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bs. ${"%.2f".format(total)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}
