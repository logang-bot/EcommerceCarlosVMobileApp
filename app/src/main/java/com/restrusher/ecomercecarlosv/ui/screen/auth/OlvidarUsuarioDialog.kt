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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Shield
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
 * Confirms wiping the remembered user from this device. Destructive: the fingerprint enrolment and
 * the stored session both go, and the next sign-in needs an email and password.
 */
@Composable
internal fun OlvidarUsuarioDialog(
    userName: String,
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
                WarningBadge()
                Spacer(modifier = Modifier.height(14.dp))
                DialogTexts(userName)
                Spacer(modifier = Modifier.height(14.dp))
                KeepsAccountNote()
                Spacer(modifier = Modifier.height(18.dp))
                DialogActions(onConfirm = onConfirm, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun WarningBadge() {
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
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = ext.amberText,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun DialogTexts(userName: String) {
    Text(
        text = stringResource(R.string.login_olvidar_title, userName),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(9.dp))
    Text(
        text = stringResource(R.string.login_olvidar_body),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.extendedColors.text2,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun KeepsAccountNote() {
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
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = ext.text3,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = stringResource(R.string.login_olvidar_note),
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
            Text(stringResource(R.string.login_olvidar_confirm), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun OlvidarUsuarioDialogPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        OlvidarUsuarioDialog(userName = "Carlos Villarroel", onConfirm = {}, onDismiss = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OlvidarUsuarioDialogDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        OlvidarUsuarioDialog(userName = "Carlos Villarroel", onConfirm = {}, onDismiss = {})
    }
}
