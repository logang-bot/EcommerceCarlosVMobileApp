package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.Intent
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun ReadyBody(
    modifier: Modifier = Modifier,
    fileName: String,
    fileSizeKb: Int,
) {
    val ex = MaterialTheme.extendedColors
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier.size(56.dp).background(ex.greenTint, RoundedCornerShape(18.dp))
                .border(1.dp, ex.greenText.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = ex.greenText, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.reporte_status_listo_heading), fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.reporte_status_listo_subtitulo), fontSize = 13.5.sp, color = ex.text2, textAlign = TextAlign.Center, lineHeight = 19.sp)
        Spacer(Modifier.height(26.dp))
        ReportDocPreview()
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(ex.surface2, RoundedCornerShape(15.dp))
                .border(1.dp, ex.border, RoundedCornerShape(15.dp)).padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(19.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(fileName, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.reporte_status_meta_html, fileSizeKb), fontSize = 12.sp, color = ex.text3)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun ReadyBottomBar(
    modifier: Modifier = Modifier,
    cachedFilePath: String?,
    fileName: String,
    downloaded: Boolean,
    onDownloaded: () -> Unit,
) {
    val ex = MaterialTheme.extendedColors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = {
                val path = cachedFilePath ?: return@OutlinedButton
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"; putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, null))
            },
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.reporte_status_accion_compartir), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
        }
        Button(
            onClick = {
                val path = cachedFilePath ?: return@Button
                scope.launch {
                    val html = withContext(Dispatchers.IO) { File(path).readText() }
                    val result = saveReportToDownloads(context, html, fileName)
                    showSaveToast(context, result)
                    if (result is SaveResult.Success) onDownloaded()
                }
            },
            enabled = !downloaded,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (downloaded) ex.greenTint else MaterialTheme.colorScheme.primary,
                contentColor = if (downloaded) ex.greenText else MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = ex.greenTint,
                disabledContentColor = ex.greenText,
            ),
        ) {
            Icon(if (downloaded) Icons.Default.Check else Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(if (downloaded) R.string.reporte_status_descargado else R.string.reporte_status_accion_descargar), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReadyBodyPreview() {
    EcomerceCarlosVTheme {
        ReadyBody(fileName = "Reporte_Diario_20260613_1432.html", fileSizeKb = 48)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ReadyBodyDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ReadyBody(fileName = "Reporte_Diario_20260613_1432.html", fileSizeKb = 48)
    }
}
