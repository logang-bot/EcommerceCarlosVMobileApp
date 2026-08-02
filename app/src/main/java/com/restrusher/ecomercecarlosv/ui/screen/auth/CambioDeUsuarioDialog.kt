package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

/**
 * Confirms letting a different account take over a device that still holds unsynced work.
 *
 * Destructive: those pedidos exist nowhere but this phone, and the incoming user cannot send them
 * because the server would file them under the wrong account. Cancelling keeps them and backs the
 * sign-in out.
 */
@Composable
internal fun CambioDeUsuarioDialog(
    incomingUserName: String,
    previousUserName: String,
    pendingCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UnsyncedBadge()
                Spacer(modifier = Modifier.height(14.dp))
                DialogTexts(
                    incomingUserName = incomingUserName,
                    previousUserName = previousUserName,
                    pendingCount = pendingCount,
                )
                Spacer(modifier = Modifier.height(14.dp))
                AskThemToSyncNote(previousUserName)
                Spacer(modifier = Modifier.height(18.dp))
                DialogActions(onConfirm = onConfirm, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun UnsyncedBadge() {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ext.amberTint),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = ext.amberText,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun DialogTexts(
    incomingUserName: String,
    previousUserName: String,
    pendingCount: Int,
) {
    Text(
        text = stringResource(R.string.login_cambio_usuario_title, incomingUserName),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(9.dp))
    Text(
        text = stringResource(R.string.login_cambio_usuario_body, pendingCount, previousUserName),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.extendedColors.text2,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun AskThemToSyncNote(previousUserName: String) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ext.surface2)
            .border(1.dp, ext.border, shape)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = ext.text3,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = stringResource(R.string.login_cambio_usuario_note, previousUserName),
            style = MaterialTheme.typography.bodySmall,
            color = ext.text3,
        )
    }
}

@Composable
private fun DialogActions(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(13.dp),
        ) {
            Text(stringResource(R.string.common_cancelar), fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(stringResource(R.string.login_cambio_usuario_confirm), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun CambioDeUsuarioDialogPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        CambioDeUsuarioDialog(
            incomingUserName = "Ana Rodríguez",
            previousUserName = "Carlos Villarroel",
            pendingCount = 7,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CambioDeUsuarioDialogDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        CambioDeUsuarioDialog(
            incomingUserName = "Ana Rodríguez",
            previousUserName = "Carlos Villarroel",
            pendingCount = 7,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
