package com.restrusher.ecomercecarlosv.ui.screen.pedido

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.DetallePedido
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PayChip
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePedidoScreen(
    navController: NavController,
    viewModel: DetallePedidoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.showPagoSheet) {
        PagoParcialSheet(
            remaining = state.pedido?.pending ?: 0.0,
            isSaving = state.isSaving,
            onSubmit = viewModel::onRegistrarPago,
            onDismiss = viewModel::onDismissPagoSheet,
        )
    }
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteConfirm,
            title = { Text(stringResource(R.string.detalle_pedido_menu_eliminar)) },
            text = { Text(stringResource(R.string.detalle_pedido_eliminar_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onDeletePedido { navController.popBackStack() } }) {
                    Text(stringResource(R.string.common_eliminar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteConfirm) {
                    Text(stringResource(R.string.common_cancelar))
                }
            },
        )
    }
    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.pedido?.createdAt)
        DatePickerDialog(
            onDismissRequest = viewModel::onDismissDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMidnight ->
                        val localMidnight = utcMidnight - TimeZone.getDefault().getOffset(utcMidnight)
                        viewModel.onUpdateDate(localMidnight)
                    } ?: viewModel.onDismissDatePicker()
                }) { Text(stringResource(R.string.common_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDatePicker) {
                    Text(stringResource(R.string.common_cancelar))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
    DetallePedidoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onMarcarPagado = viewModel::onMarcarPagado,
        onPagoParcial = viewModel::onShowPagoSheet,
        onDelete = viewModel::onShowDeleteConfirm,
        onModifyDate = viewModel::onShowDatePicker,
    )
}

@Composable
private fun DetallePedidoContent(
    state: DetallePedidoUiState,
    onBack: () -> Unit,
    onMarcarPagado: () -> Unit,
    onPagoParcial: () -> Unit,
    onDelete: () -> Unit,
    onModifyDate: () -> Unit,
) {
    val pedido = state.pedido
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.detalle_pedido_title),
                subtitle = pedido?.let {
                    SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es")).format(Date(it.createdAt))
                },
                onBack = onBack,
                actions = {
                    PedidoOverflowMenu(onDelete = onDelete, onModifyDate = onModifyDate)
                },
            )
        },
        bottomBar = {
            if (pedido != null && pedido.status != PedidoStatus.PAID) {
                DetallePedidoBottomBar(
                    isSaving = state.isSaving,
                    onMarcarPagado = onMarcarPagado,
                    onPagoParcial = onPagoParcial,
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(Modifier.padding(innerPadding).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            pedido != null -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                PedidoStatusStrip(pedido = pedido, detalleCount = state.detalles.size, clienteName = state.clienteName)
                HorizontalDivider(color = MaterialTheme.extendedColors.border)
                if (pedido.isSaldoExtra) {
                    SaldoExtraBody(description = pedido.notes.orEmpty())
                } else {
                    LineItemsSection(detalles = state.detalles)
                }
                HorizontalDivider(color = MaterialTheme.extendedColors.border)
                TotalBlock(pedido = pedido)
                if (pedido.paid > 0) {
                    HorizontalDivider(color = MaterialTheme.extendedColors.border)
                    PagosSection(pedido = pedido)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PedidoStatusStrip(pedido: Pedido, detalleCount: Int, clienteName: String) {
    val subtitle = if (pedido.isSaldoExtra) {
        stringResource(R.string.detalle_pedido_saldo_extra_label, clienteName)
    } else {
        pluralStringResource(R.plurals.detalle_pedido_n_productos, detalleCount, detalleCount, clienteName)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PayChip(status = pedido.status)
        Text(subtitle, fontSize = 12.5.sp, color = MaterialTheme.extendedColors.text3)
    }
}

@Composable
private fun SaldoExtraBody(description: String) {
    if (description.isNotBlank()) {
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.extendedColors.text2,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }
}

@Composable
internal fun DetalleSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun PedidoOverflowMenu(
    onDelete: () -> Unit,
    onModifyDate: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }
    Box {
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
            modifier = Modifier.width(220.dp),
            containerColor = ext.elevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ext.border2),
            shadowElevation = 16.dp,
        ) {
            PedidoMenuItem(
                icon = Icons.Default.CalendarToday,
                label = stringResource(R.string.detalle_pedido_menu_modificar_fecha),
            ) { expanded = false; onModifyDate() }
            PedidoMenuItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.detalle_pedido_menu_eliminar),
                isDestructive = true,
            ) { expanded = false; onDelete() }
        }
    }
}

@Composable
private fun PedidoMenuItem(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else ext.text2,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val previewPedido = Pedido(
    id = "1", clienteId = "c1", status = PedidoStatus.PARTIAL,
    total = 112.00, paid = 50.00, createdAt = 1748390400000L,
)
private val previewDetalles = listOf(
    DetallePedido("d1", "1", "p1", "Harina PAN 1kg", 12, 2.50, 2.50),
    DetallePedido("d2", "1", "p2", "Aceite Vatel 1L", 6, 4.50, 4.50),
    DetallePedido("d3", "1", "p3", "Café Madrid 250g", 4, 3.00, 5.20, "Descuento acordado"),
)
private val previewState = DetallePedidoUiState(
    pedido = previewPedido, detalles = previewDetalles,
    clienteName = "Ana Rodríguez", isLoading = false,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DetallePedidoContentPreview() {
    EcomerceCarlosVTheme {
        DetallePedidoContent(state = previewState, onBack = {}, onMarcarPagado = {}, onPagoParcial = {}, onDelete = {}, onModifyDate = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetallePedidoContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DetallePedidoContent(state = previewState, onBack = {}, onMarcarPagado = {}, onPagoParcial = {}, onDelete = {}, onModifyDate = {})
    }
}
