package com.restrusher.ecomercecarlosv.ui.screen.pedido

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun DetallePedidoBottomBar(
    isSaving: Boolean,
    onMarcarPagado: () -> Unit,
    onPagoParcial: () -> Unit,
) {
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
            modifier = Modifier.weight(1f).heightIn(min = 50.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.pedidos_pago_parcial), fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onMarcarPagado,
            enabled = !isSaving,
            modifier = Modifier.weight(1f).heightIn(min = 50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            if (isSaving) {
                CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.detalle_pedido_marcar_pagado), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PagoParcialSheet(
    remaining: Double,
    isSaving: Boolean,
    onSubmit: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        PagoParcialSheetContent(
            remaining = remaining,
            amountText = amountText,
            onAmountChange = { amountText = it },
            isSaving = isSaving,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun PagoParcialSheetContent(
    remaining: Double,
    amountText: String,
    onAmountChange: (String) -> Unit,
    isSaving: Boolean,
    onSubmit: (Double) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var showError by remember { mutableStateOf(false) }
    LaunchedEffect(amountText) { showError = false }
    val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isAmountEmpty = amountText.isBlank()
    val isAmountTooHigh = amountText.isNotBlank() && amount > remaining
    val canConfirm = !isAmountEmpty && !isAmountTooHigh
    Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 28.dp)) {
        Text(stringResource(R.string.detalle_pedido_pago_parcial_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.detalle_pedido_pendiente_label) + ": Bs. ${"%.2f".format(remaining)}", fontSize = 13.sp, color = ext.text2)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = { Text(stringResource(R.string.detalle_pedido_monto_pago_label)) },
            prefix = { Text("Bs. ", fontFamily = FontFamily.Monospace) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showError && !canConfirm,
        )
        if (showError && !canConfirm) {
            val errorText = when {
                isAmountEmpty -> stringResource(R.string.pedidos_pago_parcial_error_vacio)
                isAmountTooHigh -> stringResource(R.string.pedidos_pago_parcial_error_maximo, "%.2f".format(remaining))
                else -> ""
            }
            if (errorText.isNotBlank()) {
                Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = { if (canConfirm) onSubmit(amount) else showError = true },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                .alpha(if (!canConfirm && !isSaving) 0.5f else 1f),
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(stringResource(R.string.detalle_pedido_registrar_pago), color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DetallePedidoBottomBarPreview() {
    EcomerceCarlosVTheme {
        DetallePedidoBottomBar(isSaving = false, onMarcarPagado = {}, onPagoParcial = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetallePedidoBottomBarDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DetallePedidoBottomBar(isSaving = false, onMarcarPagado = {}, onPagoParcial = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PagoParcialSheetContentPreview() {
    EcomerceCarlosVTheme {
        Surface {
            PagoParcialSheetContent(remaining = 62.00, amountText = "30", onAmountChange = {}, isSaving = false, onSubmit = {})
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PagoParcialSheetContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            PagoParcialSheetContent(remaining = 62.00, amountText = "30", onAmountChange = {}, isSaving = false, onSubmit = {})
        }
    }
}
