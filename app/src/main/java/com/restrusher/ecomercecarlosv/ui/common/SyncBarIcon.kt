package com.restrusher.ecomercecarlosv.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

enum class SyncIconState { SYNCED, PENDING, ERROR }

@Composable
fun SyncBarIcon(state: SyncIconState, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    val icon = when (state) {
        SyncIconState.SYNCED -> Icons.Default.CloudDone
        SyncIconState.PENDING -> Icons.Default.CloudUpload
        SyncIconState.ERROR -> Icons.Default.CloudOff
    }
    val iconTint = when (state) {
        SyncIconState.SYNCED -> MaterialTheme.colorScheme.onSurface
        SyncIconState.PENDING -> MaterialTheme.colorScheme.onSurface
        SyncIconState.ERROR -> ext.amberText
    }
    val dotColor = when (state) {
        SyncIconState.SYNCED -> null
        SyncIconState.PENDING -> ext.blue
        SyncIconState.ERROR -> ext.amber
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(21.dp),
        )
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .background(dotColor, CircleShape),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "SyncBarIcon states Light")
@Composable
private fun SyncBarIconLightPreview() {
    EcomerceCarlosVTheme {
        Surface {
            Row(modifier = Modifier.padding(8.dp)) {
                SyncBarIcon(state = SyncIconState.SYNCED, onClick = {})
                SyncBarIcon(state = SyncIconState.PENDING, onClick = {})
                SyncBarIcon(state = SyncIconState.ERROR, onClick = {})
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "SyncBarIcon states Dark")
@Composable
private fun SyncBarIconDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Row(modifier = Modifier.padding(8.dp)) {
                SyncBarIcon(state = SyncIconState.SYNCED, onClick = {})
                SyncBarIcon(state = SyncIconState.PENDING, onClick = {})
                SyncBarIcon(state = SyncIconState.ERROR, onClick = {})
            }
        }
    }
}
