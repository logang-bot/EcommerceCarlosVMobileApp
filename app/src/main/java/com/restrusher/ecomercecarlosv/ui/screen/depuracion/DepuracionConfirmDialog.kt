package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun DepuracionConfirmDialog(
    state: DepuracionUiState,
    onInputChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val isValid = state.confirmInput.trim().uppercase() == "ELIMINAR"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ext.redTint),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ext.redText, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.depuracion_confirm_title, state.recordCount ?: 0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.depuracion_confirm_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text3,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.depuracion_confirm_type_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.text3,
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.confirmInput,
                    onValueChange = onInputChanged,
                    placeholder = { Text("ELIMINAR") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isValid) ext.redText else ext.border2)
                    .clickable(enabled = isValid) { onConfirm() },
            ) {
                Text(
                    text = stringResource(R.string.depuracion_confirm_cta),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isValid) Color.White else ext.text3,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancelar), color = ext.text2)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionConfirmDialogPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionConfirmDialog(
            state = DepuracionUiState(recordCount = 47, confirmInput = ""),
            onInputChanged = {}, onConfirm = {}, onDismiss = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionConfirmDialogValidDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionConfirmDialog(
            state = DepuracionUiState(recordCount = 47, confirmInput = "ELIMINAR"),
            onInputChanged = {}, onConfirm = {}, onDismiss = {},
        )
    }
}
