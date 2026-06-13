package com.restrusher.ecomercecarlosv.ui.screen.cliente

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

private enum class UnblacklistOption { RESTORE, MARK_PAID }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuitarListaNegraSheet(
    clienteName: String,
    blacklistBalance: Double,
    isManualAmount: Boolean,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onMarkAllPaid: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        QuitarListaNegraSheetContent(
            clienteName = clienteName,
            blacklistBalance = blacklistBalance,
            isManualAmount = isManualAmount,
            onDismiss = onDismiss,
            onRestore = onRestore,
            onMarkAllPaid = onMarkAllPaid,
        )
    }
}

@Composable
private fun QuitarListaNegraSheetContent(
    clienteName: String,
    blacklistBalance: Double,
    isManualAmount: Boolean,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onMarkAllPaid: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var selected by remember { mutableStateOf(UnblacklistOption.RESTORE) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ext.border3),
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ext.greenTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.height(12.dp))

            // Title
            Text(
                stringResource(R.string.quitar_lista_negra_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            )

            Spacer(Modifier.height(5.dp))

            // Subtitle with bold client name
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.quitar_lista_negra_sheet_subtitle_prefix))
                    append(" ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append(clienteName)
                    }
                    append(stringResource(R.string.quitar_lista_negra_sheet_subtitle_suffix))
                },
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(18.dp))

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ResOpt(
                    icon = Icons.Default.History,
                    iconColor = ext.blue,
                    iconTint = ext.blueTint,
                    title = stringResource(R.string.quitar_lista_negra_restore_title),
                    sub = stringResource(R.string.quitar_lista_negra_restore_desc),
                    active = selected == UnblacklistOption.RESTORE,
                    enabled = true,
                    onClick = { selected = UnblacklistOption.RESTORE },
                )
                ResOpt(
                    icon = Icons.Default.Check,
                    iconColor = ext.greenText,
                    iconTint = ext.greenTint,
                    title = stringResource(R.string.quitar_lista_negra_mark_paid_title),
                    sub = if (isManualAmount)
                        stringResource(R.string.quitar_lista_negra_mark_paid_desc)
                    else
                        stringResource(R.string.quitar_lista_negra_mark_paid_disabled_desc),
                    active = selected == UnblacklistOption.MARK_PAID,
                    enabled = isManualAmount,
                    onClick = { if (isManualAmount) selected = UnblacklistOption.MARK_PAID },
                )
            }

            // Info banner — only when manual
            if (isManualAmount) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ext.amberTint)
                        .border(1.dp, ext.amberText.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.quitar_lista_negra_info_banner_prefix))
                            append(" ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append(formatBalance(blacklistBalance))
                            }
                            append(stringResource(R.string.quitar_lista_negra_info_banner_suffix))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.text2,
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Confirm button
            Button(
                onClick = {
                    when (selected) {
                        UnblacklistOption.RESTORE -> onRestore()
                        UnblacklistOption.MARK_PAID -> if (isManualAmount) onMarkAllPaid()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.quitar_lista_negra_confirm), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            // Cancel button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(13.dp),
            ) {
                Text(
                    stringResource(R.string.quitar_lista_negra_cancel),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun ResOpt(
    icon: ImageVector,
    iconColor: Color,
    iconTint: Color,
    title: String,
    sub: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val bgColor = when {
        !enabled -> MaterialTheme.colorScheme.surface
        active -> iconTint
        else -> ext.surface2
    }
    val borderColor = when {
        !enabled -> ext.border
        active -> iconColor
        else -> ext.border2
    }
    val borderWidth = if (active && enabled) 1.5.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!enabled) Modifier.alpha(0.55f) else Modifier)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Icon tile
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (enabled) iconTint else ext.surface3),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) iconColor else ext.text3,
                modifier = Modifier.size(19.dp),
            )
        }

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else ext.text3,
            )
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.5.sp,
                color = ext.text3,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        // Radio circle
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (active && enabled) iconColor else Color.Transparent)
                .then(
                    if (!(active && enabled)) Modifier.border(1.6.dp, ext.border3, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (active && enabled) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "QuitarListaNegraSheet — manual dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuitarListaNegraSheetManualPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            QuitarListaNegraSheetContent(
                clienteName = "Ana Rodríguez",
                blacklistBalance = 415.0,
                isManualAmount = true,
                onDismiss = {}, onRestore = {}, onMarkAllPaid = {},
            )
        }
    }
}

@Preview(name = "QuitarListaNegraSheet — auto dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuitarListaNegraSheetAutoPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            QuitarListaNegraSheetContent(
                clienteName = "Carlos Méndez",
                blacklistBalance = 200.0,
                isManualAmount = false,
                onDismiss = {}, onRestore = {}, onMarkAllPaid = {},
            )
        }
    }
}
