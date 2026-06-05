package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ClientesFilterMenu(
    modifier: Modifier = Modifier,
    active: ClienteSortMode,
    onSelect: (ClienteSortMode) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (expanded) ext.accentSoft else Color.Transparent),
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                tint = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(256.dp),
            containerColor = ext.elevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ext.border2),
            shadowElevation = 16.dp,
        ) {
            Text(
                text = stringResource(R.string.clientes_sort_label),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                color = ext.text3,
                modifier = Modifier.padding(start = 12.dp, top = 9.dp, bottom = 7.dp),
            )
            FilterItem(Icons.Default.Sort, stringResource(R.string.clientes_sort_az), stringResource(R.string.clientes_sort_az_sub), active == ClienteSortMode.AZ) {
                onSelect(ClienteSortMode.AZ); expanded = false
            }
            FilterItem(Icons.Default.Block, stringResource(R.string.clientes_sort_criticos), stringResource(R.string.clientes_sort_criticos_sub), active == ClienteSortMode.CRITICOS_FIRST) {
                onSelect(ClienteSortMode.CRITICOS_FIRST); expanded = false
            }
            FilterItem(Icons.Default.Wallet, stringResource(R.string.clientes_sort_balance), stringResource(R.string.clientes_sort_balance_sub), active == ClienteSortMode.MAYOR_SALDO) {
                onSelect(ClienteSortMode.MAYOR_SALDO); expanded = false
            }
            FilterItem(Icons.Default.Receipt, stringResource(R.string.clientes_sort_deuda), stringResource(R.string.clientes_sort_deuda_sub), active == ClienteSortMode.SOLO_CON_DEUDA) {
                onSelect(ClienteSortMode.SOLO_CON_DEUDA); expanded = false
            }
            HorizontalDivider(color = ext.border, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            FilterItem(
                icon = Icons.Default.Refresh,
                label = stringResource(R.string.clientes_sort_restablecer),
                sub = null,
                active = false,
                enabled = active != ClienteSortMode.AZ,
            ) { onSelect(ClienteSortMode.AZ); expanded = false }
        }
    }
}

@Composable
private fun FilterItem(
    icon: ImageVector,
    label: String,
    sub: String?,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) ext.accentSoft else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary else ext.text2,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.primary else if (enabled) MaterialTheme.colorScheme.onSurface else ext.text3,
            )
            if (sub != null) {
                Text(text = sub, fontSize = 11.5.sp, color = ext.text3, modifier = Modifier.padding(top = 1.dp))
            }
        }
        if (active) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }
    }
}
