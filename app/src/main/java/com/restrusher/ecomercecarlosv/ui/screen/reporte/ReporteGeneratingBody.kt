package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
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
internal fun GeneratingBody(
    modifier: Modifier = Modifier,
    pending: PendingExport?,
    step: Int,
) {
    val ex = MaterialTheme.extendedColors

    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmerTransition.animateFloat(
        initialValue = -350f, targetValue = 650f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX",
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(ex.surface2, ex.surface3, ex.surface2),
        start = Offset(shimmerX, 0f), end = Offset(shimmerX + 350f, 0f),
    )
    val syncAngle by rememberInfiniteTransition(label = "sync").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing)),
        label = "syncAngle",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = when (step) { 0 -> 0.12f; 1 -> 0.62f; 2 -> 0.95f; else -> 1f },
        animationSpec = tween(400), label = "progress",
    )

    val dataLabel = stringResource(
        if (pending?.isMovimientosVariant == true) R.string.reporte_status_paso_movimientos
        else R.string.reporte_status_paso_pedidos
    )
    val countText = pending?.let {
        stringResource(
            if (it.isMovimientosVariant) R.string.reporte_status_reuniendo_movimientos
            else R.string.reporte_status_reuniendo_pedidos,
            it.itemCount,
        )
    } ?: stringResource(R.string.reporte_status_preparando)

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier.size(width = 150.dp, height = 197.dp).clip(RoundedCornerShape(9.dp))
                .background(shimmerBrush).border(1.dp, ex.border, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = ex.text4, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(26.dp))
        Text(stringResource(R.string.reporte_status_creando), fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(6.dp))
        Text(countText, fontSize = 13.5.sp, color = ex.text2, textAlign = TextAlign.Center, lineHeight = 19.sp)
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.reporte_status_progreso), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = ex.text2)
                Text("${(animatedProgress * 100).toInt()}%", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary, trackColor = ex.surface3, strokeCap = StrokeCap.Round,
            )
        }
        Spacer(Modifier.height(26.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            GenStep(state = stepStateFor(step, 0), label = dataLabel, syncAngle = syncAngle)
            GenStep(state = stepStateFor(step, 1), label = stringResource(R.string.reporte_status_paso_generando), syncAngle = syncAngle)
            GenStep(state = stepStateFor(step, 2), label = stringResource(R.string.reporte_status_paso_listo), syncAngle = syncAngle)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GenStep(state: String, label: String, syncAngle: Float) {
    val ex = MaterialTheme.extendedColors
    val (bgColor, textColor) = when (state) {
        "done"   -> ex.greenTint to ex.text2
        "active" -> ex.accentTint to MaterialTheme.colorScheme.onBackground
        else     -> ex.surface3 to ex.text3
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(modifier = Modifier.size(24.dp).background(bgColor, CircleShape), contentAlignment = Alignment.Center) {
            when (state) {
                "done"   -> Icon(Icons.Default.Check, contentDescription = null, tint = ex.greenText, modifier = Modifier.size(13.dp))
                "active" -> Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp).rotate(syncAngle))
                else     -> Box(Modifier.size(6.dp).background(ex.text4, CircleShape))
            }
        }
        Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

internal fun stepStateFor(current: Int, index: Int) = when {
    current > index  -> "done"
    current == index -> "active"
    else             -> "todo"
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun GeneratingBodyPreview() {
    EcomerceCarlosVTheme {
        GeneratingBody(pending = PendingExport("", "Reporte_Diario.html", 23, false), step = 1)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun GeneratingBodyDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        GeneratingBody(pending = PendingExport("", "Reporte_Diario.html", 38, true), step = 0)
    }
}
