package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PagoSheet
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPedidoScreen(
    navController: NavController,
    viewModel: EditarPedidoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.editedDate)
        DatePickerDialog(
            onDismissRequest = viewModel::onDismissDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMidnight ->
                        val local = utcMidnight - TimeZone.getDefault().getOffset(utcMidnight)
                        viewModel.onDateChanged(local)
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

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteConfirm,
            title = { Text(stringResource(R.string.editar_pedido_eliminar)) },
            text = { Text(stringResource(R.string.editar_pedido_eliminar_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeletePedido {
                        navController.popBackStack()
                        navController.popBackStack()
                    }
                }) {
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

    val editingIdx = state.editingLineIndex
    if (editingIdx != null && editingIdx < state.lines.size) {
        val line = state.lines[editingIdx]
        EditLineSheet(
            productName = line.productName,
            initialPrice = line.unitPrice,
            catalogPrice = line.catalogPrice,
            initialNotes = line.notes,
            onSave = { price, notes -> viewModel.onSaveLine(editingIdx, price, notes) },
            onDismiss = viewModel::onDismissEditLine,
        )
    }

    if (state.showPaymentSheet) {
        val pedido = state.pedido
        PagoSheet(
            total = state.newTotal,
            clienteName = state.clienteName,
            itemCount = state.lines.size,
            isSaving = state.isSaving,
            onSubmit = { payment -> viewModel.onSave(payment) { navController.popBackStack() } },
            onDismiss = viewModel::onDismissPaymentSheet,
            initialStatus = pedido?.status ?: PedidoStatus.PENDING,
            initialPaidAmount = pedido?.paid ?: 0.0,
            ctaLabel = stringResource(R.string.editar_pedido_guardar),
        )
    }

    EditarPedidoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onShowDatePicker = viewModel::onShowDatePicker,
        onQtyDecrement = { idx -> viewModel.onQuantityChange(idx, -1) },
        onQtyIncrement = { idx -> viewModel.onQuantityChange(idx, +1) },
        onRemoveLine = viewModel::onRemoveLine,
        onEditLine = viewModel::onShowEditLine,
        onDelete = viewModel::onShowDeleteConfirm,
        onShowPaymentSheet = viewModel::onShowPaymentSheet,
    )
}

@Composable
private fun EditarPedidoContent(
    state: EditarPedidoUiState,
    onBack: () -> Unit,
    onShowDatePicker: () -> Unit,
    onQtyDecrement: (Int) -> Unit,
    onQtyIncrement: (Int) -> Unit,
    onRemoveLine: (Int) -> Unit,
    onEditLine: (Int) -> Unit,
    onDelete: () -> Unit,
    onShowPaymentSheet: () -> Unit,
) {
    val pedido = state.pedido
    val dateFormat = remember { SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.editar_pedido_title),
                subtitle = if (pedido != null && state.clienteName.isNotBlank()) {
                    SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es")).format(Date(state.editedDate)) + " · " + state.clienteName
                } else null,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (!state.isLoading && pedido != null) {
                EditarPedidoBottomBar(
                    total = state.newTotal,
                    isSaving = state.isSaving,
                    onSave = onShowPaymentSheet,
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
                if (pedido.paid > 0) {
                    PagoInfoBanner(
                        statusLabel = when (pedido.status.name) {
                            "PAID" -> "pagado"
                            "PARTIAL" -> "parcial"
                            else -> "pendiente"
                        },
                        paid = pedido.paid,
                    )
                }
                DateField(
                    date = state.editedDate,
                    dateFormat = dateFormat,
                    onClick = onShowDatePicker,
                )
                LinesSection(
                    lines = state.lines,
                    onQtyDecrement = onQtyDecrement,
                    onQtyIncrement = onQtyIncrement,
                    onRemoveLine = onRemoveLine,
                    onEditLine = onEditLine,
                )
                DangerZone(onDelete = onDelete)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PagoInfoBanner(statusLabel: String, paid: Double) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ext.blueTint)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Default.Receipt, contentDescription = null, tint = ext.blueText, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        val annotated = buildAnnotatedString {
            append("Pedido ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
            append(statusLabel)
            pop()
            append(" — ya tiene ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
            append("Bs. ${"%.2f".format(paid)}")
            pop()
            append(" abonados. Editar las cantidades recalculará el saldo restante.")
        }
        Text(annotated, fontSize = 12.5.sp, color = ext.text2, lineHeight = 18.sp)
    }
}

@Composable
private fun DateField(date: Long, dateFormat: java.text.DateFormat, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(
            text = stringResource(R.string.editar_pedido_fecha_section).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ext.text3,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            Text(
                text = dateFormat.format(Date(date)),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun LinesSection(
    lines: List<EditLineState>,
    onQtyDecrement: (Int) -> Unit,
    onQtyIncrement: (Int) -> Unit,
    onRemoveLine: (Int) -> Unit,
    onEditLine: (Int) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "${lines.size} productos".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ext.text3,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
        )
        lines.forEachIndexed { index, line ->
            if (index > 0) {
                HorizontalDivider(color = ext.border, modifier = Modifier.padding(start = 72.dp))
            }
            EditOrderLineRow(
                line = line,
                onDecrement = { onQtyDecrement(index) },
                onIncrement = { onQtyIncrement(index) },
                onRemove = { onRemoveLine(index) },
                onEditPrice = { onEditLine(index) },
            )
        }
    }
}

@Composable
private fun EditOrderLineRow(
    line: EditLineState,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onRemove: () -> Unit,
    onEditPrice: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ext.surface3)
                    .border(1.dp, ext.border, RoundedCornerShape(11.dp)),
            ) {
                Icon(Icons.Default.Sell, contentDescription = null, tint = ext.text3, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(line.productName, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onEditPrice)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = "Bs. ${"%.2f".format(line.unitPrice)}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = if (line.isPriceModified) ext.amberText else ext.text2,
                    )
                    if (line.isPriceModified) {
                        Text(
                            text = "%.2f".format(line.catalogPrice),
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ext.text3,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                }
            }
            Text(
                text = "Bs. ${"%.2f".format(line.subtotal)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            )
        }
        Row(
            modifier = Modifier.padding(top = 12.dp, start = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LineQuantityStepper(qty = line.quantity, onDecrement = onDecrement, onIncrement = onIncrement)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(ext.redTint)
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.22f), RoundedCornerShape(11.dp))
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 14.dp)
                    .height(38.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ext.redText, modifier = Modifier.size(15.dp))
                Text(stringResource(R.string.editar_pedido_quitar), color = ext.redText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (!line.notes.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .padding(top = 12.dp, start = 52.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border, RoundedCornerShape(11.dp))
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = ext.text3, modifier = Modifier.size(14.dp))
                Text("\"${line.notes}\"", fontSize = 12.5.sp, color = ext.text2, fontStyle = FontStyle.Italic, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun LineQuantityStepper(qty: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border2, RoundedCornerShape(11.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null, tint = ext.text2, modifier = Modifier.size(17.dp))
        }
        Text(
            text = "$qty",
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onIncrement, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun DangerZone(onDelete: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        HorizontalDivider(color = ext.border, modifier = Modifier.padding(vertical = 16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(ext.redTint)
                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(13.dp))
                .clickable(onClick = onDelete),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = ext.redText, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text(stringResource(R.string.editar_pedido_eliminar), color = ext.redText, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = stringResource(R.string.editar_pedido_eliminar_confirm),
            fontSize = 12.sp,
            color = ext.text3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EditarPedidoBottomBar(total: Double, isSaving: Boolean, onSave: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, ext.border, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.editar_pedido_nuevo_total).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ext.text3,
                letterSpacing = 0.4.sp,
            )
            Text(
                text = "Bs. ${"%.2f".format(total)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Button(
            onClick = onSave,
            enabled = !isSaving,
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.5.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.editar_pedido_guardar), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ── Edit Price/Notes bottom sheet ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLineSheet(
    productName: String,
    initialPrice: Double,
    catalogPrice: Double,
    initialNotes: String?,
    onSave: (price: Double, notes: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var priceText by remember { mutableStateOf("%.2f".format(initialPrice)) }
    var notes by remember { mutableStateOf(initialNotes ?: "") }

    val price = priceText.replace(",", ".").toDoubleOrNull() ?: initialPrice
    val priceModified = price != catalogPrice
    val ext = MaterialTheme.extendedColors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(ext.surface3),
                ) {
                    Icon(Icons.Default.Sell, contentDescription = null, tint = ext.text3, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text(productName, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
                    Text("Precio de catálogo · Bs. ${"%.2f".format(catalogPrice)}", fontSize = 12.5.sp, color = ext.text3)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.pedidos_precio_unitario_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("Bs.", fontSize = 14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            if (priceModified) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(ext.amberTint)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Tag, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(16.dp))
                    Column {
                        Text(stringResource(R.string.pedidos_precio_modificado), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ext.amberText)
                        Text(
                            text = stringResource(R.string.pedidos_precio_modificado_desc, "%.2f".format(catalogPrice), "%.2f".format(price)),
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.pedidos_nota_label), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.pedidos_nota_optional), fontSize = 13.sp, color = ext.text3)
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                placeholder = { Text(stringResource(R.string.pedidos_nota_placeholder), color = ext.text3) },
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.pedidos_subtotal_linea), fontSize = 13.sp, color = ext.text2)
                Text("Bs. ${"%.2f".format(price)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = (-0.5).sp)
            }
            Spacer(Modifier.height(14.dp))
            TextButton(
                onClick = { onSave(price, notes.ifBlank { null }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Text(stringResource(R.string.common_confirmar), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}
