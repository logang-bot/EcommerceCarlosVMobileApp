package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.res.Configuration
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DepuracionDatePickerDialog(
    initialDateMs: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMs)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let(onConfirm)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionDatePickerDialogPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionDatePickerDialog(
            initialDateMs = System.currentTimeMillis(),
            onConfirm = {}, onDismiss = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionDatePickerDialogDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionDatePickerDialog(
            initialDateMs = System.currentTimeMillis(),
            onConfirm = {}, onDismiss = {},
        )
    }
}
