package com.restrusher.ecomercecarlosv.ui.screen.pedido

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
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
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 28.dp)) {
            TotalHeader(total = total, clienteName = clienteName, itemCount = itemCount)
            Spacer(Modifier.height(16.dp))

            PaymentOptionRow(
                icon = Icons.Default.CheckCircle,
                iconTint = ext.greenText,
                background = ext.greenTint,
                title = stringResource(R.string.pedidos_marcar_pagado),
                subtitle = stringResource(R.string.pedidos_pago_total_hint),
                selected = selected == PaymentOption.PAID,
                onClick = { selected = PaymentOption.PAID },
            )
            Spacer(Modifier.height(10.dp))
            PaymentOptionRow(
                icon = Icons.Default.MonetizationOn,
                iconTint = MaterialTheme.colorScheme.primary,
                background = ext.accentSoft,
                title = stringResource(R.string.pedidos_pago_parcial),
                subtitle = stringResource(R.string.pedidos_pago_parcial_hint),
                selected = selected == PaymentOption.PARTIAL,
                onClick = { selected = PaymentOption.PARTIAL },
                bordered = selected == PaymentOption.PARTIAL,
            )
            if (selected == PaymentOption.PARTIAL) {
                Spacer(Modifier.height(8.dp))
                PartialAmountInput(
                    text = partialAmountText,
                    onValueChange = { partialAmountText = it },
                    total = total,
                )
            }
            Spacer(Modifier.height(10.dp))
            PaymentOptionRow(
                icon = Icons.Default.Tag,
                iconTint = ext.amberText,
                background = ext.amberTint,
                title = stringResource(R.string.pedidos_dejar_pendiente),
                subtitle = stringResource(R.string.pedidos_dejar_pendiente_hint),
                selected = selected == PaymentOption.PENDING,
                onClick = { selected = PaymentOption.PENDING },
            )
            Spacer(Modifier.height(20.dp))

            val initialPayment = when (selected) {
                PaymentOption.PAID -> total
                PaymentOption.PARTIAL -> partialAmountText.replace(",", ".").toDoubleOrNull()?.coerceAtMost(total) ?: 0.0
                PaymentOption.PENDING -> 0.0
            }
            TextButton(
                onClick = { onSubmit(initialPayment) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(stringResource(R.string.pedidos_registrar), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun TotalHeader(total: Double, clienteName: String, itemCount: Int) {
    val ext = MaterialTheme.extendedColors
    Column {
        Text(stringResource(R.string.pedidos_confirmar_titulo), fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
        Text("Bs. ${"%.2f".format(total)}", fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = (-1).sp)
        Text("$itemCount productos · $clienteName", fontSize = 13.sp, color = ext.text2)
    }
}

@Composable
private fun PaymentOptionRow(
    icon: ImageVector,
    iconTint: Color,
    background: Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    bordered: Boolean = false,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val borderMod = if (bordered) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)) else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = ext.text2)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(20.dp).clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary else ext.surface3),
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
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        prefix = { Text("Bs.", fontSize = 14.sp) },
        suffix = { Text("Restan Bs. ${"%.2f".format(remaining)}", fontSize = 11.sp, color = ext.text3) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
    )
}
