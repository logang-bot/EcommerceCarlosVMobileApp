package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun DepuracionProgressContent(
    state: DepuracionUiState,
    isExporting: Boolean,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    val progressColor = if (isExporting) accent else ext.redText
    val iconTint = if (isExporting) ext.greenTint else ext.redTint
    val labelRes = if (isExporting) R.string.depuracion_phase1_label else R.string.depuracion_phase2_label

    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(300),
        label = "progress",
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.depuracion_title),
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
            PhaseSteps(activePhase = if (isExporting) 1 else 2)

            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(iconTint),
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = progressColor, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = progressColor,
                trackColor = ext.surface2,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "${state.currentCount} / ${state.totalCount}",
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionProgressContentExportingPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionProgressContent(
            state = DepuracionUiState(progress = 0.4f, currentCount = 20, totalCount = 50),
            isExporting = true,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionProgressContentDeletingDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionProgressContent(
            state = DepuracionUiState(progress = 0.8f, currentCount = 40, totalCount = 50),
            isExporting = false,
        )
    }
}
