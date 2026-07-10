package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme

@Composable
fun DepuracionScreen(
    navController: NavController,
    viewModel: DepuracionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DepuracionContent(
        state = state,
        onBack = { navController.popBackStack() },
        onCutoffClick = viewModel::onShowDatePicker,
        onCutoffChanged = viewModel::onCutoffChanged,
        onDismissDatePicker = viewModel::onDismissDatePicker,
        onFormatSelected = viewModel::onFormatSelected,
        onExportarClick = viewModel::onExportarClick,
        onConfirmInputChanged = viewModel::onConfirmInputChanged,
        onConfirmDelete = viewModel::onConfirmDelete,
        onCancelConfirm = viewModel::onCancelConfirm,
        onRetry = viewModel::onRetry,
    )
}

@Composable
internal fun DepuracionContent(
    state: DepuracionUiState,
    onBack: () -> Unit,
    onCutoffClick: () -> Unit,
    onCutoffChanged: (Long) -> Unit,
    onDismissDatePicker: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit,
    onExportarClick: () -> Unit,
    onConfirmInputChanged: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelConfirm: () -> Unit,
    onRetry: () -> Unit,
) {
    BackHandler(enabled = state.phase == DepuracionPhase.EXPORTING || state.phase == DepuracionPhase.DELETING) {
        // Block back during active operations
    }

    when (state.phase) {
        DepuracionPhase.CONFIG -> DepuracionConfigContent(
            state = state,
            onBack = onBack,
            onCutoffClick = onCutoffClick,
            onFormatSelected = onFormatSelected,
            onExportarClick = onExportarClick,
        )
        DepuracionPhase.EXPORTING -> DepuracionProgressContent(
            state = state,
            isExporting = true,
        )
        DepuracionPhase.DELETING -> DepuracionProgressContent(
            state = state,
            isExporting = false,
        )
        DepuracionPhase.DONE -> DepuracionDoneContent(
            state = state,
            onBack = onBack,
        )
        DepuracionPhase.ERROR -> DepuracionErrorContent(
            state = state,
            onRetry = onRetry,
            onCancel = onBack,
        )
    }

    if (state.showDatePicker) {
        DepuracionDatePickerDialog(
            initialDateMs = state.cutoffDateMs,
            onConfirm = onCutoffChanged,
            onDismiss = onDismissDatePicker,
        )
    }

    if (state.showConfirmDialog) {
        DepuracionConfirmDialog(
            state = state,
            onInputChanged = onConfirmInputChanged,
            onConfirm = onConfirmDelete,
            onDismiss = onCancelConfirm,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DepuracionContentConfigPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DepuracionContent(
            state = DepuracionUiState(recordCount = 47),
            onBack = {}, onCutoffClick = {}, onCutoffChanged = {}, onDismissDatePicker = {},
            onFormatSelected = {}, onExportarClick = {}, onConfirmInputChanged = {},
            onConfirmDelete = {}, onCancelConfirm = {}, onRetry = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DepuracionContentDoneDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DepuracionContent(
            state = DepuracionUiState(
                phase = DepuracionPhase.DONE,
                deletedCount = 47,
                exportedFileName = "pedidos_hasta_2025-07-10.xlsx",
                exportedFileSize = 182_400L,
            ),
            onBack = {}, onCutoffClick = {}, onCutoffChanged = {}, onDismissDatePicker = {},
            onFormatSelected = {}, onExportarClick = {}, onConfirmInputChanged = {},
            onConfirmDelete = {}, onCancelConfirm = {}, onRetry = {},
        )
    }
}
