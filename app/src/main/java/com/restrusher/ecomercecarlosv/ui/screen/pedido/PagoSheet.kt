package com.restrusher.ecomercecarlosv.ui.screen.pedido

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

private enum class PaymentOption { PAID, PARTIAL, PENDING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoSheet(
    total: Double,
    clienteName: String,
    itemCount: Int,
    isSaving: Boolean,
    onSubmit: (initialPayment: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(PaymentOption.PENDING) }
    var partialAmountText by remember { mutableStateOf("") }
    val ext = MaterialTheme.extendedColors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        PagoSheetContent(
            total = total,
            clienteName = clienteName,
            itemCount = itemCount,
            isSaving = isSaving,
            selected = selected,
            partialAmountText = partialAmountText,
            onSelectOption = { selected = it },
            onPartialAmountChange = { partialAmountText = it },
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun PagoSheetContent(
    total: Double,
    clienteName: String,
    itemCount: Int,
    isSaving: Boolean,
    selected: PaymentOption,
    partialAmountText: String,
    onSelectOption: (PaymentOption) -> Unit,
    onPartialAmountChange: (String) -> Unit,
    onSubmit: (Double) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 28.dp)) {
        TotalHeader(total = total, clienteName = clienteName, itemCount = itemCount)
        Spacer(Modifier.height(16.dp))

        PaymentOptionRow(
            icon = Icons.Default.Check,
            activeColor = ext.green,
            tintColor = ext.greenTint,
            title = stringResource(R.string.pedidos_marcar_pagado),
            subtitle = stringResource(R.string.pedidos_pago_total_hint),
            selected = selected == PaymentOption.PAID,
            onClick = { onSelectOption(PaymentOption.PAID) },
        )
        Spacer(Modifier.height(10.dp))
        PaymentOptionRow(
            icon = Icons.Default.Payments,
            activeColor = MaterialTheme.colorScheme.primary,
            tintColor = ext.accentTint,
            title = stringResource(R.string.pedidos_pago_parcial),
            subtitle = stringResource(R.string.pedidos_pago_parcial_hint),
            selected = selected == PaymentOption.PARTIAL,
            onClick = { onSelectOption(PaymentOption.PARTIAL) },
        )
        if (selected == PaymentOption.PARTIAL) {
            Spacer(Modifier.height(8.dp))
            PartialAmountInput(
                text = partialAmountText,
                onValueChange = onPartialAmountChange,
                total = total,
            )
        }
        Spacer(Modifier.height(10.dp))
        PaymentOptionRow(
            icon = Icons.Default.Tag,
            activeColor = ext.amber,
            tintColor = ext.amberTint,
            title = stringResource(R.string.pedidos_dejar_pendiente),
            subtitle = stringResource(R.string.pedidos_dejar_pendiente_hint),
            selected = selected == PaymentOption.PENDING,
            onClick = { onSelectOption(PaymentOption.PENDING) },
        )
        Spacer(Modifier.height(20.dp))

        val partialAmount = partialAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
        val initialPayment = when (selected) {
            PaymentOption.PAID -> total
            PaymentOption.PARTIAL -> partialAmount.coerceAtMost(total)
            PaymentOption.PENDING -> 0.0
        }
        val canConfirm = selected != PaymentOption.PARTIAL || partialAmount > 0.0
        TextButton(
            onClick = { onSubmit(initialPayment) },
            enabled = !isSaving && canConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(stringResource(R.string.pedidos_registrar), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun TotalHeader(total: Double, clienteName: String, itemCount: Int) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.pedidos_confirmar_titulo), fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp, textAlign = TextAlign.Center)
        Text("Bs. ${"%.2f".format(total)}", fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = (-1).sp, modifier = Modifier.padding(top = 8.dp))
        Text("$itemCount productos · $clienteName", fontSize = 13.sp, color = ext.text2, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun PaymentOptionRow(
    icon: ImageVector,
    activeColor: Color,
    tintColor: Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val rowBg = if (selected) tintColor else ext.surface2
    val shape = RoundedCornerShape(15.dp)
    val borderMod = if (selected) {
        Modifier.border(1.5.dp, activeColor, shape)
    } else {
        Modifier.border(1.dp, ext.border, shape)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(rowBg)
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tintColor),
        ) {
            Icon(icon, contentDescription = null, tint = activeColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.5.sp, color = ext.text3, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) activeColor else Color.Transparent)
                .then(if (!selected) Modifier.border(1.6.dp, ext.border3, CircleShape) else Modifier),
        ) {
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun PartialAmountInput(text: String, onValueChange: (String) -> Unit, total: Double) {
    val ext = MaterialTheme.extendedColors
    val paid = text.replace(",", ".").toDoubleOrNull() ?: 0.0
    val remaining = (total - paid).coerceAtLeast(0.0)
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(ext.surface2)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Bs.", fontSize = 15.sp, color = ext.text2, fontWeight = FontWeight.Medium)
        BasicTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        Text("Restan Bs. ${"%.2f".format(remaining)}", fontSize = 12.5.sp, color = ext.text3)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PagoSheetPendingPreview() {
    EcomerceCarlosVTheme {
        Surface {
            PagoSheetContent(
                total = 85.50, clienteName = "Ana Rodríguez", itemCount = 4,
                isSaving = false, selected = PaymentOption.PENDING, partialAmountText = "",
                onSelectOption = {}, onPartialAmountChange = {}, onSubmit = {},
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PagoSheetPendingDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            PagoSheetContent(
                total = 85.50, clienteName = "Ana Rodríguez", itemCount = 4,
                isSaving = false, selected = PaymentOption.PENDING, partialAmountText = "",
                onSelectOption = {}, onPartialAmountChange = {}, onSubmit = {},
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PagoSheetPartialPreview() {
    EcomerceCarlosVTheme {
        Surface {
            PagoSheetContent(
                total = 85.50, clienteName = "Ana Rodríguez", itemCount = 4,
                isSaving = false, selected = PaymentOption.PARTIAL, partialAmountText = "40.00",
                onSelectOption = {}, onPartialAmountChange = {}, onSubmit = {},
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PagoSheetPaidPreview() {
    EcomerceCarlosVTheme {
        Surface {
            PagoSheetContent(
                total = 85.50, clienteName = "Ana Rodríguez", itemCount = 4,
                isSaving = false, selected = PaymentOption.PAID, partialAmountText = "",
                onSelectOption = {}, onPartialAmountChange = {}, onSubmit = {},
            )
        }
    }
}
