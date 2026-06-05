package com.restrusher.ecomercecarlosv.ui.common

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.restrusher.ecomercecarlosv.R

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (isPermanentlyDeclined) {
                        onGoToAppSettingsClick()
                    } else {
                        onOkClick()
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        if (isPermanentlyDeclined) R.string.permission_grant else R.string.ok
                    ),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        title = {
            Text(text = stringResource(R.string.permission_required))
        },
        text = {
            Text(
                text = stringResource(permissionTextProvider.getDescription(isPermanentlyDeclined))
            )
        },
    )
}

interface PermissionTextProvider {
    @StringRes
    fun getDescription(isPermanentlyDeclined: Boolean): Int
}

class CameraPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): Int {
        return if (isPermanentlyDeclined) {
            R.string.camera_permission_permanently_declined
        } else {
            R.string.camera_permission_rationale
        }
    }
}
