package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun FailedBody(
    modifier: Modifier = Modifier,
    isNoSpace: Boolean,
) {
    val ex = MaterialTheme.extendedColors
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier.size(56.dp).background(ex.redTint, RoundedCornerShape(18.dp))
                .border(1.dp, ex.redText.copy(alpha = 0.28f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = ex.redText, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.reporte_status_error_heading), fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(if (isNoSpace) R.string.reporte_status_error_sin_espacio_subtitulo else R.string.reporte_status_error_generico_subtitulo),
            fontSize = 13.5.sp, color = ex.text2, textAlign = TextAlign.Center, lineHeight = 19.5.sp,
        )
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth().background(ex.surface2, RoundedCornerShape(15.dp))
                .border(1.dp, ex.border, RoundedCornerShape(15.dp)).padding(15.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Error, contentDescription = null, tint = ex.redText, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(if (isNoSpace) R.string.reporte_status_error_sin_espacio_label else R.string.reporte_status_error_escritura_label),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(top = 11.dp, bottom = 11.dp), color = ex.border, thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.reporte_status_error_codigo), fontSize = 12.5.sp, color = ex.text3)
                Text(
                    text = if (isNoSpace) "ERR_ALMACEN · ENOSPC" else "ERR_ESCRITURA",
                    fontSize = 12.5.sp, color = ex.text2, fontFamily = FontFamily.Monospace,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(ex.surface2, RoundedCornerShape(12.dp))
                .border(1.dp, ex.border, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = ex.text3, modifier = Modifier.size(15.dp).padding(top = 1.dp))
            Text(stringResource(R.string.reporte_status_error_reassurance), fontSize = 12.sp, color = ex.text2, lineHeight = 17.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun FailedBottomBar(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(15.dp)) {
            Text(stringResource(R.string.common_cancelar), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
        }
        Button(onClick = onRetry, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(15.dp)) {
            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.reporte_status_accion_reintentar), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun FailedBodyNoSpacePreview() {
    EcomerceCarlosVTheme { FailedBody(isNoSpace = true) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FailedBodyDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) { FailedBody(isNoSpace = false) }
}
