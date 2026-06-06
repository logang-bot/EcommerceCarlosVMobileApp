package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
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
    var showDatePicker by remember { mutableStateOf(false) }
    val dateLabel = remember(state.date) {
        SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date(state.date))
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
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = { SaldoExtraSaveBar(enabled = state.canSave, isSaving = state.isSaving, onSave = onSave) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SaldoExtraCategoryField()
            SaldoExtraAmountHero(
                value = state.amount,
                isError = state.amountError,
                onValueChange = onAmountChange,
            )
            SaldoExtraDescriptionField(
                value = state.description,
                isError = state.descriptionError,
                onValueChange = onDescriptionChange,
            )
            SaldoExtraDateField(label = dateLabel, onClick = { showDatePicker = true })
        }
    }
}

@Composable
private fun SaldoExtraSaveBar(enabled: Boolean, isSaving: Boolean, onSave: () -> Unit) {
    Box(
        modifier = Modifier
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
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(R.string.saldo_extra_guardar), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val previewSaldoState = SaldoExtraUiState(
    clienteName = "Ana Rodríguez",
    amount = "60,00",
    description = "Envases retornables no devueltos",
    date = 1748908800000L,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun SaldoExtraContentPreview() {
    EcomerceCarlosVTheme {
        SaldoExtraContent(
            state = previewSaldoState, onBack = {},
            onDescriptionChange = {}, onAmountChange = {}, onDateChange = {}, onSave = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SaldoExtraContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        SaldoExtraContent(
            state = previewSaldoState, onBack = {},
            onDescriptionChange = {}, onAmountChange = {}, onDateChange = {}, onSave = {},
        )
    }
}
