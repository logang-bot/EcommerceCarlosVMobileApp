package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun PedidosSection(
    pedidos: List<Pedido>,
    allPedidosCount: Int,
    activeFilters: Set<PedidoStatus>,
    onClearFilters: () -> Unit,
    onPedidoClick: (String) -> Unit,
    onNuevoPedidoClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.extendedColors
    val actionLabel = if (activeFilters.isNotEmpty()) "${pedidos.size} de $allPedidosCount" else null

    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = if (activeFilters.isEmpty()) 8.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.detalle_cliente_cuenta_section).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ext.text3,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                fontSize = 11.sp,
                color = ext.text3,
            )
        }
    }

    if (activeFilters.isNotEmpty()) {
        FilterChipsRow(activeFilters = activeFilters, onClearFilters = onClearFilters)
    }

    if (pedidos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Receipt,
            title = stringResource(R.string.detalle_cliente_pedidos_empty_title),
            subtitle = stringResource(R.string.detalle_cliente_pedidos_empty_subtitle),
            hint = if (activeFilters.isEmpty()) stringResource(R.string.detalle_cliente_pedidos_empty_hint) else null,
            compact = true,
            modifier = Modifier.height(240.dp),
            onActionClick = if (activeFilters.isEmpty()) onNuevoPedidoClick else null,
        )
    } else {
        pedidos.forEachIndexed { index, pedido ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 75.dp),
                    color = ext.border,
                )
            }
            PedidoRow(pedido = pedido, onClick = { onPedidoClick(pedido.id) })
        }
    }
}

@Composable
private fun FilterChipsRow(
    activeFilters: Set<PedidoStatus>,
    onClearFilters: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        activeFilters.forEach { status -> StatusFilterChip(status = status) }
        ClearChip(onClick = onClearFilters)
    }
}

@Composable
private fun StatusFilterChip(status: PedidoStatus) {
    val ext = MaterialTheme.extendedColors
    val (chipColor, chipTint, dotColor, label) = when (status) {
        PedidoStatus.PENDING -> StatusChipStyle(ext.blueText, ext.blueTint, Color(0xFF4C8DF5), stringResource(R.string.pedidos_estado_pendiente))
        PedidoStatus.PARTIAL -> StatusChipStyle(ext.amberText, ext.amberTint, Color(0xFFE7B23E), stringResource(R.string.pedidos_estado_parcial))
        PedidoStatus.PAID    -> StatusChipStyle(ext.greenText, ext.greenTint, Color(0xFF36C880), stringResource(R.string.pedidos_estado_pagado))
    }
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(CircleShape)
            .background(chipTint)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(dotColor))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = chipColor)
    }
}

@Composable
private fun ClearChip(onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(CircleShape)
            .background(ext.surface2)
            .border(1.dp, ext.border2, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = ext.text3, modifier = Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.detalle_cliente_filtros_limpiar),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ext.text3,
        )
    }
}

@Composable
internal fun PedidosMenuButton(
    modifier: Modifier = Modifier,
    activeFilters: Set<PedidoStatus>,
    onToggleFilter: (PedidoStatus) -> Unit,
    onClearFilters: () -> Unit,
    onGenerarReporte: () -> Unit = {},
) {
    val ext = MaterialTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }
    val hasFilters = activeFilters.isNotEmpty()

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (expanded || hasFilters) ext.accentSoft else Color.Transparent),
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                tint = if (expanded || hasFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(248.dp),
            containerColor = ext.elevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ext.border2),
            shadowElevation = 16.dp,
        ) {
            ReporteMenuItem(onClick = { onGenerarReporte(); expanded = false })
            MenuSectionHeader(stringResource(R.string.detalle_cliente_menu_filtrar_header))
            PedidoStatusMenuItems(activeFilters = activeFilters, onToggleFilter = onToggleFilter)
            HorizontalDivider(color = ext.border, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            ResetMenuItem(enabled = hasFilters) { onClearFilters(); expanded = false }
        }
    }
}

@Composable
private fun ReporteMenuItem(onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(ext.accentSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(ext.accentTint),
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.detalle_cliente_menu_generar_label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.detalle_cliente_menu_generar_sub), fontSize = 11.5.sp, color = ext.text3, modifier = Modifier.padding(top = 1.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun MenuSectionHeader(text: String) {
    val ext = MaterialTheme.extendedColors
    HorizontalDivider(color = ext.border, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    Text(
        text = text.uppercase(),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
        color = ext.text3,
        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun PedidoStatusMenuItems(
    activeFilters: Set<PedidoStatus>,
    onToggleFilter: (PedidoStatus) -> Unit,
) {
    val items = listOf(
        Triple(PedidoStatus.PENDING, Color(0xFF4C8DF5), stringResource(R.string.pedidos_estado_pendiente)),
        Triple(PedidoStatus.PARTIAL, Color(0xFFE7B23E), stringResource(R.string.pedidos_estado_parcial)),
        Triple(PedidoStatus.PAID,    Color(0xFF36C880), stringResource(R.string.pedidos_estado_pagado)),
    )
    items.forEach { (status, dotColor, label) ->
        val active = status in activeFilters
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(if (active) MaterialTheme.extendedColors.accentSoft else Color.Transparent)
                .clickable { onToggleFilter(status) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            if (active) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun ResetMenuItem(enabled: Boolean, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null, tint = if (enabled) ext.text2 else ext.text4, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.detalle_cliente_menu_restablecer),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else ext.text3,
        )
    }
}

private data class StatusChipStyle(val color: Color, val tint: Color, val dot: Color, val label: String)

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewPedidos = listOf(
    Pedido(id = "o1", clienteId = "c1", status = PedidoStatus.PENDING, total = 47.50, paid = 0.0, createdAt = 1748822400000L, itemCount = 3),
    Pedido(id = "o2", clienteId = "c1", status = PedidoStatus.PARTIAL, total = 112.00, paid = 50.0, createdAt = 1748390400000L, itemCount = 5),
    Pedido(id = "o3", clienteId = "c1", status = PedidoStatus.PAID, total = 24.00, paid = 24.0, createdAt = 1747872000000L, itemCount = 2),
    Pedido(id = "o4", clienteId = "c1", status = PedidoStatus.PENDING, total = 60.00, paid = 0.0, createdAt = 1747353600000L, isSaldoExtra = true, notes = "Envases retornables"),
)

@Preview(name = "PedidosSection — no filters dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidosSectionPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column {
                PedidosSection(
                    pedidos = previewPedidos,
                    allPedidosCount = previewPedidos.size,
                    activeFilters = emptySet(),
                    
                    onClearFilters = {},
                    onPedidoClick = {},
                )
            }
        }
    }
}

@Preview(name = "PedidosSection — with filters dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidosSectionWithFiltersPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column {
                val filtered = previewPedidos.filter { it.status == PedidoStatus.PENDING }
                PedidosSection(
                    pedidos = filtered,
                    allPedidosCount = previewPedidos.size,
                    activeFilters = setOf(PedidoStatus.PENDING),
                    
                    onClearFilters = {},
                    onPedidoClick = {},
                )
            }
        }
    }
}

@Preview(name = "PedidosSection — empty dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidosSectionEmptyPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column {
                PedidosSection(
                    pedidos = emptyList(),
                    allPedidosCount = 0,
                    activeFilters = emptySet(),
                    
                    onClearFilters = {},
                    onPedidoClick = {},
                    onNuevoPedidoClick = {},
                )
            }
        }
    }
}
