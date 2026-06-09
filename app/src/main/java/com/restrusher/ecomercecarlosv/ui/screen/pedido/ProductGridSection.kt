package com.restrusher.ecomercecarlosv.ui.screen.pedido

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Producto
import com.restrusher.ecomercecarlosv.ui.common.PhotoThumbnail
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ProductGridSection(
    modifier: Modifier = Modifier,
    productos: List<Producto>,
    cartQuantities: (String) -> Int,
    onToggleProduct: (Producto) -> Unit,
    onAdjustQuantity: (String, Int) -> Unit,
    onSearchClick: () -> Unit,
    bottomPadding: Dp = 0.dp,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = bottomPadding + 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SearchCell(onClick = onSearchClick) }
        items(productos, key = { it.id }) { producto ->
            val qty = cartQuantities(producto.id)
            ProductCard(
                producto = producto,
                quantity = qty,
                onToggle = { onToggleProduct(producto) },
                onIncrement = { onAdjustQuantity(producto.id, +1) },
                onDecrement = { onAdjustQuantity(producto.id, -1) },
            )
        }
    }
}

@Composable
private fun SearchCell(onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, ext.border2, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(ext.surface3),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ext.surface2),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
            Text(
                text = stringResource(R.string.pedidos_buscar_producto),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ext.text2,
                modifier = Modifier.height(29.dp),
            )
            Text(text = "", fontSize = 12.5.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ProductCard(
    producto: Producto,
    quantity: Int,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val inCart = quantity > 0
    val borderColor = if (inCart) MaterialTheme.colorScheme.primary else ext.border
    val borderWidth = if (inCart) 1.6.dp else 1.dp

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ext.surface2)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
        ) {
            PhotoThumbnail(
                photoUrl = producto.photoUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .background(ext.surface3),
                fallback = {
                    Icon(Icons.Default.Sell, contentDescription = null, tint = ext.text3, modifier = Modifier.size(28.dp))
                },
            )
            if (inCart) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
            Text(
                text = producto.name,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(29.dp),
            )
            if (inCart) {
                QuantityStepper(quantity = quantity, onIncrement = onIncrement, onDecrement = onDecrement)
            } else {
                Text(
                    text = "Bs. ${"%.2f".format(producto.price)}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(ext.accentSoft),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
        Text(
            text = "$quantity",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
        )
        IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
    }
}

private val previewProductos = listOf(
    Producto(id = "1", name = "Arroz premium", price = 12.50, createdAt = 0),
    Producto(id = "2", name = "Aceite girasol 1L", price = 18.00, createdAt = 0),
    Producto(id = "3", name = "Azúcar blanca", description = "Bolsa 1kg", price = 8.50, createdAt = 0),
    Producto(id = "4", name = "Fideos tallarín", price = 6.00, createdAt = 0),
    Producto(id = "5", name = "Leche evaporada", price = 4.50, createdAt = 0),
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ProductGridSectionPreview() {
    EcomerceCarlosVTheme {
        ProductGridSection(
            productos = previewProductos,
            cartQuantities = { if (it == "1") 2 else 0 },
            onToggleProduct = {},
            onAdjustQuantity = { _, _ -> },
            onSearchClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ProductGridSectionDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ProductGridSection(
            productos = previewProductos,
            cartQuantities = { 0 },
            onToggleProduct = {},
            onAdjustQuantity = { _, _ -> },
            onSearchClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun SearchCellPreview() {
    EcomerceCarlosVTheme {
        SearchCell(onClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SearchCellDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        SearchCell(onClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ProductCardPreview() {
    EcomerceCarlosVTheme {
        ProductCard(
            producto = previewProductos.first(),
            quantity = 0,
            onToggle = {},
            onIncrement = {},
            onDecrement = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ProductCardDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ProductCard(
            producto = previewProductos.first(),
            quantity = 2,
            onToggle = {},
            onIncrement = {},
            onDecrement = {},
        )
    }
}
