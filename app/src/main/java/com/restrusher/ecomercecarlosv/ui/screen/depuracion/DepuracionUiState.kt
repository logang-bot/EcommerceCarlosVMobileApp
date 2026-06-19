package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import android.net.Uri
import java.util.Calendar

enum class ExportFormat { CSV, XLSX }

enum class DepuracionPhase { CONFIG, EXPORTING, DELETING, DONE, ERROR }

data class DepuracionUiState(
    val cutoffDateMs: Long = defaultCutoff(),
    val format: ExportFormat = ExportFormat.XLSX,
    val recordCount: Int? = null,
    val isCountLoading: Boolean = false,
    val phase: DepuracionPhase = DepuracionPhase.CONFIG,
    val showConfirmDialog: Boolean = false,
    val confirmInput: String = "",
    val showDatePicker: Boolean = false,
    val progress: Float = 0f,
    val currentCount: Int = 0,
    val totalCount: Int = 0,
    val exportedFileName: String? = null,
    val exportedFileSize: Long = 0L,
    val exportedFileUri: Uri? = null,
    val errorMessage: String? = null,
    val deletedCount: Int = 0,
)

private fun defaultCutoff(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.YEAR, -1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
