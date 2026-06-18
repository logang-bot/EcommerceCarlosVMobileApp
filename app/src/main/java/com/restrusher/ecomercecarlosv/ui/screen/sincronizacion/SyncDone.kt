package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun SyncDone(lastSyncedAt: Long?, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(ext.greenTint)
                .border(1.dp, ext.greenText.copy(alpha = 0.24f), RoundedCornerShape(26.dp)),
        ) {
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = ext.greenText,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.sinc_done_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.sinc_done_subtitle),
            fontSize = 13.5.sp,
            color = ext.text2,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 250.dp),
        )
        Spacer(Modifier.height(16.dp))
        val timeLabel = if (lastSyncedAt != null) {
            stringResource(R.string.sinc_last_sync, relativeTime(lastSyncedAt))
        } else {
            stringResource(R.string.sinc_never)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, ext.border, RoundedCornerShape(999.dp))
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ext.greenText,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = timeLabel,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ext.text3,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "SyncDone Light")
@Composable
private fun SyncDonePreview() {
    EcomerceCarlosVTheme {
        Surface {
            SyncDone(lastSyncedAt = System.currentTimeMillis() - 5 * 60_000)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "SyncDone Dark")
@Composable
private fun SyncDoneDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            SyncDone(lastSyncedAt = null)
        }
    }
}
