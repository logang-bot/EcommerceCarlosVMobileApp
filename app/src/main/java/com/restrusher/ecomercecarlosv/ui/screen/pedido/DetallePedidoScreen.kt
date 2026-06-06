package com.restrusher.ecomercecarlosv.ui.screen.pedido

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PayChip
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetallePedidoScreen(
    navController: NavController,
    viewModel: DetallePedidoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.showPagoSheet) {
        PagoParacialSheet(
            remaining = state.pedido?.pending ?: 0.0,
            isSaving = state.isSaving,
            onSubmit = viewModel::onRegistrarPago,
            onDismiss = viewModel::onDismissPagoSheet,
        )
    }
    DetallePedidoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onMarcarPagado = viewModel::onMarcarPagado,
        onPagoParcial = viewModel::onShowPagoSheet,
    )
}

@Composable
private fun DetallePedidoContent(
    state: DetallePedidoUiState,
    onBack: () -> Unit,
    onMarcarPagado: () -> Unit,
    onPagoParcial: () -> Unit,
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
                actions = { if (pedido != null) PayChip(status = pedido.status) },
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
                Spacer(Modifier.height(8.dp))
                DetalleSectionLabel(stringResource(R.string.detalle_pedido_section_lineas))
                if (pedido.isSaldoExtra) {
                    SaldoExtraInfo(description = pedido.notes.orEmpty())
                } else {
                    LineItemsSection(detalles = state.detalles)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.extendedColors.border)
                Spacer(Modifier.height(12.dp))
                TotalBlock(pedido = pedido)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SaldoExtraInfo(description: String) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        if (description.isNotBlank()) {
            Text(description, fontSize = 14.sp, color = ext.text2)
        }
    }
}

@Composable
private fun TotalBlock(pedido: Pedido) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        TotalRow(label = stringResource(R.string.detalle_pedido_total_label), amount = pedido.total, color = null)
        if (pedido.paid > 0) {
            Spacer(Modifier.height(6.dp))
            TotalRow(label = stringResource(R.string.detalle_pedido_pagado_label), amount = pedido.paid, color = ext.greenText)
        }
        if (pedido.pending > 0) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = ext.border)
            Spacer(Modifier.height(4.dp))
            TotalRow(label = stringResource(R.string.detalle_pedido_pendiente_label), amount = pedido.pending, color = ext.amberText)
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color?) {
    val ext = MaterialTheme.extendedColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = color ?: ext.text2, fontWeight = FontWeight.Medium)
        Text(
            text = "Bs. ${"%.2f".format(amount)}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = color ?: MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun DetallePedidoBottomBar(isSaving: Boolean, onMarcarPagado: () -> Unit, onPagoParcial: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedButton(
            onClick = onPagoParcial,
            enabled = !isSaving,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text(stringResource(R.string.pedidos_registrar_pago_parcial), fontWeight = FontWeight.SemiBold) }
        Button(
            onClick = onMarcarPagado,
            enabled = !isSaving,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.extendedColors.greenText),
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(stringResource(R.string.pedidos_marcar_pagado), fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PagoParacialSheet(
    remaining: Double,
    isSaving: Boolean,
    onSubmit: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    val ext = MaterialTheme.extendedColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 28.dp)) {
            Text(stringResource(R.string.detalle_pedido_pago_parcial_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.detalle_pedido_pendiente_label) + ": Bs. ${"%.2f".format(remaining)}", fontSize = 13.sp, color = ext.text2)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.detalle_pedido_monto_pago_label)) },
                prefix = { Text("Bs. ", fontFamily = FontFamily.Monospace) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(16.dp))
            val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
            TextButton(
                onClick = { onSubmit(amount.coerceAtMost(remaining)) },
                enabled = amount > 0 && !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.detalle_pedido_registrar_pago), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DetalleSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
    )
}
