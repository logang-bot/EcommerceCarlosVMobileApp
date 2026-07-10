package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun DepuracionDoneContent(
    state: DepuracionUiState,
    onBack: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PhaseSteps(activePhase = 3)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ext.greenTint),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.depuracion_done_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.depuracion_done_body, state.deletedCount),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                textAlign = TextAlign.Center,
            )

            // File card
            if (state.exportedFileName != null) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                        .background(ext.surface2)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.exportedFileName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ext.text2,
                        )
                        Text(
                            text = formatFileSize(state.exportedFileSize),
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.text3,
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ext.surface3)
                            .clickable {
                                state.exportedFileUri?.let { uri ->
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            },
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ext.text2, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent)
                    .clickable { onBack() },
            ) {
                Text(
                    text = stringResource(R.string.depuracion_done_cta),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionDoneContentPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionDoneContent(
            state = DepuracionUiState(
                deletedCount = 47,
                exportedFileName = "pedidos_hasta_2025-07-10.xlsx",
                exportedFileSize = 182_400L,
                exportedFileUri = Uri.EMPTY,
            ),
            onBack = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionDoneContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionDoneContent(
            state = DepuracionUiState(
                deletedCount = 12,
                exportedFileName = "pedidos_hasta_2025-07-10.csv",
                exportedFileSize = 8_200L,
                exportedFileUri = Uri.EMPTY,
            ),
            onBack = {},
        )
    }
}
