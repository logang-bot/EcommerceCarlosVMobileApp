package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaldoExtraScreen(
    navController: NavController,
    viewModel: SaldoExtraViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SaldoExtraContent(
        state = state,
        onBack = { navController.popBackStack() },
        onDescriptionChange = viewModel::onDescriptionChange,
        onAmountChange = viewModel::onAmountChange,
        onDateChange = viewModel::onDateChange,
        onSave = { viewModel.onSave { navController.popBackStack() } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaldoExtraContent(
    state: SaldoExtraUiState,
    onBack: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSave: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var showDatePicker by remember { mutableStateOf(false) }
    val dateLabel = remember(state.date) {
        SimpleDateFormat("dd/MM/yyyy", Locale("es")).format(Date(state.date))
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onDateChange)
                    showDatePicker = false
                }) { Text(stringResource(R.string.common_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancelar))
                }
            },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.saldo_extra_title),
                subtitle = state.clienteName.ifBlank { null },
                onBack = onBack,
            )
        },
        bottomBar = { SaldoExtraSaveBar(enabled = state.canSave, isSaving = state.isSaving, onSave = onSave) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(12.dp))
            SaldoExtraSectionLabel(stringResource(R.string.saldo_extra_categoria))
            SaldoExtraCategoryField()
            Spacer(Modifier.height(20.dp))
            SaldoExtraSectionLabel(stringResource(R.string.saldo_extra_descripcion))
            SaldoExtraDescriptionField(
                value = state.description,
                isError = state.descriptionError,
                onValueChange = onDescriptionChange,
            )
            Spacer(Modifier.height(20.dp))
            SaldoExtraSectionLabel(stringResource(R.string.saldo_extra_monto))
            SaldoExtraAmountField(
                value = state.amount,
                isError = state.amountError,
                onValueChange = onAmountChange,
            )
            Spacer(Modifier.height(20.dp))
            SaldoExtraSectionLabel(stringResource(R.string.saldo_extra_fecha))
            SaldoExtraDateField(label = dateLabel, onClick = { showDatePicker = true })
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SaldoExtraCategoryField() {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Default.Tag, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.saldo_extra_categoria), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.Lock, contentDescription = null, tint = ext.text3, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun SaldoExtraDescriptionField(value: String, isError: Boolean, onValueChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().defaultMinSize(minHeight = 100.dp),
        shape = RoundedCornerShape(14.dp),
        placeholder = { Text(stringResource(R.string.saldo_extra_descripcion_placeholder), color = ext.text3) },
        isError = isError,
        supportingText = if (isError) ({ Text(stringResource(R.string.saldo_extra_descripcion_error)) }) else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = ext.border2,
            unfocusedBorderColor = ext.border,
        ),
    )
}

@Composable
private fun SaldoExtraAmountField(value: String, isError: Boolean, onValueChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        placeholder = { Text("0,00", color = ext.text3, fontFamily = FontFamily.Monospace) },
        prefix = { Text("Bs. ", fontFamily = FontFamily.Monospace, color = ext.text2) },
        isError = isError,
        supportingText = if (isError) ({ Text(stringResource(R.string.saldo_extra_monto_error)) }) else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = ext.border2,
            unfocusedBorderColor = ext.border,
        ),
    )
}

@Composable
private fun SaldoExtraDateField(label: String, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border2, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SaldoExtraSaveBar(enabled: Boolean, isSaving: Boolean, onSave: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Button(
            onClick = onSave,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(15.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.saldo_extra_guardar), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            }
        }
    }
}

@Composable
private fun SaldoExtraSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
    )
}
