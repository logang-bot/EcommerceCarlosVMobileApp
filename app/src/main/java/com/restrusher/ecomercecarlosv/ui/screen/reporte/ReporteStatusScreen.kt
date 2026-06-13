package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private sealed interface ExportStatus {
    data object Generating : ExportStatus
    data object Ready : ExportStatus
    data class Failed(val isNoSpace: Boolean) : ExportStatus
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteStatusScreen(navController: NavController) {
    val pending = remember { ReporteExportHolder.pending }

    var retryKey by remember { mutableIntStateOf(0) }
    var step by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf<ExportStatus>(ExportStatus.Generating) }
    var cachedFilePath by remember { mutableStateOf<String?>(null) }
    var fileSizeKb by remember { mutableIntStateOf(0) }
    var downloaded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(retryKey) {
        if (pending == null) { navController.popBackStack(); return@LaunchedEffect }
        status = ExportStatus.Generating; step = 0; cachedFilePath = null
        try {
            delay(400L); step = 1
            val file = withContext(Dispatchers.IO) {
                File(context.cacheDir, "reports").also { it.mkdirs() }
                    .let { File(it, pending.fileName).also { f -> f.writeText(pending.html, Charsets.UTF_8) } }
            }
            fileSizeKb = (file.length() / 1024).toInt().coerceAtLeast(1)
            delay(350L); step = 2; delay(200L)
            cachedFilePath = file.absolutePath
            ReporteExportHolder.pending = null
            status = ExportStatus.Ready
        } catch (e: IOException) {
            val m = e.message.orEmpty()
            status = ExportStatus.Failed(m.contains("ENOSPC") || m.contains("No space left"))
        } catch (e: Exception) {
            status = ExportStatus.Failed(false)
        }
    }

    val topBarTitle = when (status) {
        is ExportStatus.Ready   -> stringResource(R.string.reporte_status_listo_title)
        is ExportStatus.Failed  -> stringResource(R.string.reporte_status_error_title)
        else                    -> stringResource(R.string.reporte_status_generando_title)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (status == ExportStatus.Generating) {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(
                                stringResource(R.string.common_cancelar),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.extendedColors.text2,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            when (status) {
                is ExportStatus.Ready -> ReadyBottomBar(
                    cachedFilePath = cachedFilePath,
                    fileName = pending?.fileName ?: "",
                    downloaded = downloaded,
                    onDownloaded = { downloaded = true },
                )
                is ExportStatus.Failed -> FailedBottomBar(
                    onRetry = { retryKey++ },
                    onBack = { navController.popBackStack() },
                )
                else -> {}
            }
        },
    ) { innerPadding ->
        when (val s = status) {
            is ExportStatus.Generating -> GeneratingBody(
                pending = pending,
                step = step,
                modifier = Modifier.padding(innerPadding),
            )
            is ExportStatus.Ready -> ReadyBody(
                fileName = pending?.fileName ?: "",
                fileSizeKb = fileSizeKb,
                modifier = Modifier.padding(innerPadding),
            )
            is ExportStatus.Failed -> FailedBody(
                isNoSpace = s.isNoSpace,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReporteStatusScreenPreview() {
    EcomerceCarlosVTheme {
        ReporteStatusScreen(navController = rememberNavController())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ReporteStatusScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ReporteStatusScreen(navController = rememberNavController())
    }
}
