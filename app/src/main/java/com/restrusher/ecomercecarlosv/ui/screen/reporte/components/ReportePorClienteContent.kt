package com.restrusher.ecomercecarlosv.ui.screen.reporte.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ClienteOption
import com.restrusher.ecomercecarlosv.ui.screen.reporte.HistorialItem
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteUiState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.PedidosExtendedColors
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ClienteSelectorCard(
    name: String,
    photoUrl: String?,
    mercadoName: String,
    onCambiar: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ClienteAvatar(name = name, photoUrl = photoUrl, size = 44.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.ifBlank { stringResource(R.string.reporte_selector_cliente) },
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (mercadoName.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(text = mercadoName, fontSize = 12.sp, color = ext.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(ext.accentSoft)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(9.dp))
                .clickable(onClick = onCambiar)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.reporte_cambiar_cliente),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteSelectorSheet(
    clientes: List<ClienteOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.reporte_selector_cliente),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
            HorizontalDivider(color = ext.border)
            clientes.forEach { cliente ->
                val isSelected = cliente.id == selectedId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) ext.accentSoft else Color.Transparent)
                        .clickable { onSelect(cliente.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ClienteAvatar(name = cliente.name, photoUrl = cliente.photoUrl, size = 38.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cliente.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (cliente.mercadoName.isNotBlank()) {
                            Text(cliente.mercadoName, fontSize = 12.sp, color = ext.text2, maxLines = 1)
                        }
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(start = 70.dp), color = ext.border)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun ClienteStatCards(state: ReporteUiState) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReporteStatCard(
            label = stringResource(R.string.reporte_facturado),
            value = "Bs. ${"%.2f".format(state.facturado)}",
            valueColor = MaterialTheme.colorScheme.primary,
            iconBgColor = ext.accentTint,
            icon = Icons.Default.ShoppingCart,
            modifier = Modifier.weight(1f),
        )
        ReporteStatCard(
            label = stringResource(R.string.reporte_pagado),
            value = "Bs. ${"%.2f".format(state.pagado)}",
            valueColor = ext.greenText,
            iconBgColor = ext.greenTint,
            icon = Icons.Default.Check,
            modifier = Modifier.weight(1f),
        )
        ReporteStatCard(
            label = stringResource(R.string.reporte_saldo),
            value = "Bs. ${"%.2f".format(state.saldo)}",
            valueColor = ext.amberText,
            iconBgColor = ext.amberTint,
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun HistorialSectionHeader() {
    Text(
        text = stringResource(R.string.reporte_historial).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
    )
}

@Composable
fun EmptyHistorial() {
    Text(
        text = stringResource(R.string.reporte_sin_historial),
        fontSize = 13.sp,
        color = MaterialTheme.extendedColors.text3,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    )
}

@Composable
fun HistorialRow(item: HistorialItem) {
    val ext = MaterialTheme.extendedColors
    val (iconVec, iconBg, iconTint) = historialIconSpec(item, ext)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
        ) {
            Icon(iconVec, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (item.pending > 0) "Bs. ${"%.2f".format(item.pending)} pendiente" else "Pagado",
                fontSize = 12.sp,
                color = if (item.pending > 0) ext.amberText else ext.greenText,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Bs. ${"%.2f".format(item.total)}",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                letterSpacing = (-0.3).sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (item.isSaldoExtra) "Extra" else "Total",
                fontSize = 11.sp,
                color = ext.text3,
            )
        }
    }
}

private data class IconSpec(val icon: ImageVector, val bg: Color, val tint: Color)

@Composable
private fun historialIconSpec(item: HistorialItem, ext: PedidosExtendedColors): IconSpec = when {
    item.isSaldoExtra -> IconSpec(Icons.Default.Assessment, ext.amberTint, ext.amberText)
    item.pending == 0.0 -> IconSpec(Icons.Default.Check, ext.greenTint, ext.greenText)
    else -> IconSpec(Icons.Default.Receipt, ext.accentSoft, MaterialTheme.colorScheme.primary)
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewHistorial = listOf(
    HistorialItem("1", "12 Jun 2026", 0L, 120.0, 120.0, 0.0, false),
    HistorialItem("2", "10 Jun 2026", 0L, 85.0, 30.0, 55.0, false),
    HistorialItem("3", "Saldo extra", 0L, 45.0, 0.0, 45.0, true),
)

private val previewClienteState = ReporteUiState(
    facturado = 980.0,
    pagado = 650.0,
    saldo = 330.0,
    selectedClienteName = "Ana Rodríguez",
    selectedMercadoName = "Mercado Central",
    isLoading = false,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ClienteSelectorCardPreview() {
    EcomerceCarlosVTheme {
        ClienteSelectorCard(
            name = "Ana Rodríguez",
            photoUrl = null,
            mercadoName = "Mercado Central",
            onCambiar = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ClienteStatCardsPreview() {
    EcomerceCarlosVTheme {
        ClienteStatCards(state = previewClienteState)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HistorialRowsDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Column {
            previewHistorial.forEach { HistorialRow(item = it) }
        }
    }
}
