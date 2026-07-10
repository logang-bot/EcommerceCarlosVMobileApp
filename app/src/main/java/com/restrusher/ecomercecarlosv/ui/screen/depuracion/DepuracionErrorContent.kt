package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
internal fun DepuracionErrorContent(
    state: DepuracionUiState,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
                onBack = onCancel,
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
            PhaseSteps(activePhase = 1, hasFailed = true)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ext.redTint),
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = ext.redText, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.depuracion_error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.depuracion_error_body),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ext.greenTint)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
            ) {
                Text(
                    text = stringResource(R.string.depuracion_error_safe_pill),
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.greenText,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text4,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, ext.border2, RoundedCornerShape(14.dp))
                        .clickable { onCancel() },
                ) {
                    Text(
                        text = stringResource(R.string.common_cancelar),
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.text2,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ext.redText)
                        .clickable { onRetry() },
                ) {
                    Text(
                        text = stringResource(R.string.depuracion_retry),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionErrorContentPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionErrorContent(
            state = DepuracionUiState(errorMessage = "Sin conexión a internet"),
            onRetry = {}, onCancel = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionErrorContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionErrorContent(
            state = DepuracionUiState(errorMessage = null),
            onRetry = {}, onCancel = {},
        )
    }
}
