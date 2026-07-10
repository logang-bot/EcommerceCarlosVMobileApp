package com.restrusher.ecomercecarlosv.ui.screen.depuracion

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun PhaseSteps(
    activePhase: Int,
    modifier: Modifier = Modifier,
    hasFailed: Boolean = false,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepCircle(number = 1, label = "Exportar", active = activePhase >= 1, done = activePhase > 1, failed = hasFailed && activePhase == 1)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(if (activePhase > 1) accent else ext.border2),
        )
        StepCircle(number = 2, label = "Eliminar", active = activePhase >= 2, done = activePhase > 2, failed = hasFailed && activePhase == 2)
    }
}

@Composable
private fun StepCircle(
    number: Int,
    label: String,
    active: Boolean,
    done: Boolean,
    failed: Boolean,
) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    val bgColor = when {
        failed -> ext.redText
        done || active -> accent
        else -> ext.surface2
    }
    val fgColor = when {
        failed || done || active -> MaterialTheme.colorScheme.onPrimary
        else -> ext.text3
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.dp, if (!active && !done && !failed) ext.border2 else Color.Transparent, CircleShape),
        ) {
            if (done) {
                Icon(Icons.Default.Check, contentDescription = null, tint = fgColor, modifier = Modifier.size(16.dp))
            } else {
                Text(text = number.toString(), color = fgColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (active || done) MaterialTheme.colorScheme.onBackground else ext.text3, fontSize = 11.sp)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PhaseStepsInProgressPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        PhaseSteps(activePhase = 1, modifier = Modifier.padding(vertical = 16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PhaseStepsFailedDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        PhaseSteps(activePhase = 1, hasFailed = true, modifier = Modifier.padding(vertical = 16.dp))
    }
}
