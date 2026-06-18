package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.SyncIconState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun SyncBanner(state: SincronizacionUiState, onRetry: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    val isError = state.syncState == SyncIconState.ERROR
    val bgColor = if (isError) ext.amberTint else ext.blueTint
    val ringColor = if (isError) ext.amber else ext.blue
    val iconTint = if (isError) ext.amberText else ext.blueText
    val icon = if (isError) Icons.Default.CloudOff else Icons.Default.CloudUpload
    val title = if (isError) {
        stringResource(R.string.sinc_error_title, state.items.size)
    } else {
        stringResource(R.string.sinc_pending_title, state.items.size)
    }
    val body = if (isError) {
        stringResource(R.string.sinc_error_body)
    } else {
        stringResource(R.string.sinc_pending_body)
    }

    Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)) {
        val iconBoxColor = if (isError) ext.amber.copy(alpha = 0.18f) else ext.blue.copy(alpha = 0.18f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, ringColor.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(iconBoxColor)
                        .border(1.dp, ringColor.copy(alpha = 0.24f), RoundedCornerShape(13.dp)),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = body,
                        fontSize = 12.5.sp,
                        color = ext.text2,
                        lineHeight = (12.5 * 1.45).sp,
                    )
                }
            }
            if (isError) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sinc_retry),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val pendingState = SincronizacionUiState(
    syncState = SyncIconState.PENDING,
    items = List(3) {
        SyncQueueItem(
            id = it.toLong(),
            entityType = "PEDIDO",
            operation = "UPSERT",
            entityLabel = "Bs. 120,00",
            createdAt = System.currentTimeMillis() - 2 * 60_000,
            retryCount = 0,
        )
    },
)

private val errorState = pendingState.copy(
    syncState = SyncIconState.ERROR,
    items = pendingState.items.map { it.copy(retryCount = 1) },
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "SyncBanner Pending Light")
@Composable
private fun SyncBannerPendingPreview() {
    EcomerceCarlosVTheme {
        Surface { SyncBanner(state = pendingState, onRetry = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "SyncBanner Pending Dark")
@Composable
private fun SyncBannerPendingDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SyncBanner(state = pendingState, onRetry = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "SyncBanner Error Light")
@Composable
private fun SyncBannerErrorPreview() {
    EcomerceCarlosVTheme {
        Surface { SyncBanner(state = errorState, onRetry = {}) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "SyncBanner Error Dark")
@Composable
private fun SyncBannerErrorDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SyncBanner(state = errorState, onRetry = {}) }
    }
}
