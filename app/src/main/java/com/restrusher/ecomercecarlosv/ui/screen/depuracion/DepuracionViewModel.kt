package com.restrusher.ecomercecarlosv.ui.screen.depuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.repository.CleanupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepuracionViewModel @Inject constructor(
    private val cleanupRepository: CleanupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DepuracionUiState())
    val state: StateFlow<DepuracionUiState> = _state.asStateFlow()

    private var countJob: Job? = null
    private var depuracionJob: Job? = null

    init {
        loadCount()
    }

    fun onCutoffChanged(ms: Long) {
        _state.update { it.copy(cutoffDateMs = ms, showDatePicker = false) }
        loadCount()
    }

    fun onFormatSelected(format: ExportFormat) {
        _state.update { it.copy(format = format) }
    }

    fun onShowDatePicker() {
        _state.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _state.update { it.copy(showDatePicker = false) }
    }

    fun onExportarClick() {
        _state.update { it.copy(showConfirmDialog = true) }
    }

    fun onConfirmInputChanged(text: String) {
        _state.update { it.copy(confirmInput = text) }
    }

    fun onCancelConfirm() {
        _state.update { it.copy(showConfirmDialog = false, confirmInput = "") }
    }

    fun onConfirmDelete() {
        if (_state.value.confirmInput.trim().uppercase() != "ELIMINAR") return
        _state.update { it.copy(showConfirmDialog = false, confirmInput = "") }
        startDepuracion()
    }

    fun onRetry() {
        startDepuracion()
    }

    private fun loadCount() {
        countJob?.cancel()
        countJob = viewModelScope.launch {
            _state.update { it.copy(isCountLoading = true, recordCount = null) }
            runCatching { cleanupRepository.countPedidosOlderThan(_state.value.cutoffDateMs) }
                .onSuccess { count -> _state.update { it.copy(recordCount = count, isCountLoading = false) } }
                .onFailure { _state.update { it.copy(isCountLoading = false) } }
        }
    }

    private fun startDepuracion() {
        depuracionJob?.cancel()
        depuracionJob = viewModelScope.launch {
            val total = _state.value.recordCount ?: 0
            _state.update {
                it.copy(
                    phase = DepuracionPhase.EXPORTING,
                    progress = 0f,
                    currentCount = 0,
                    totalCount = total,
                    errorMessage = null,
                    exportedFileName = null,
                    exportedFileUri = null,
                )
            }

            val exportResult = runCatching {
                cleanupRepository.exportPedidosToFile(
                    cutoffMs = _state.value.cutoffDateMs,
                    useXlsx = _state.value.format == ExportFormat.XLSX,
                    onProgress = { current, t ->
                        _state.update {
                            it.copy(
                                progress = if (t > 0) current.toFloat() / t else 0f,
                                currentCount = current,
                                totalCount = t,
                            )
                        }
                    },
                )
            }

            if (exportResult.isFailure) {
                _state.update {
                    it.copy(
                        phase = DepuracionPhase.ERROR,
                        errorMessage = exportResult.exceptionOrNull()?.localizedMessage,
                    )
                }
                return@launch
            }

            val (fileName, fileSize, fileUri) = exportResult.getOrThrow()
            _state.update {
                it.copy(
                    phase = DepuracionPhase.DELETING,
                    progress = 0f,
                    currentCount = 0,
                    exportedFileName = fileName,
                    exportedFileSize = fileSize,
                    exportedFileUri = fileUri,
                )
            }

            val deleteResult = runCatching {
                cleanupRepository.deletePedidosFromCloud(
                    cutoffMs = _state.value.cutoffDateMs,
                    onProgress = { current, t ->
                        _state.update {
                            it.copy(
                                progress = if (t > 0) current.toFloat() / t else 0f,
                                currentCount = current,
                                totalCount = t.coerceAtLeast(1),
                            )
                        }
                    },
                )
            }

            if (deleteResult.isFailure) {
                _state.update {
                    it.copy(
                        phase = DepuracionPhase.ERROR,
                        errorMessage = deleteResult.exceptionOrNull()?.localizedMessage,
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(phase = DepuracionPhase.DONE, deletedCount = deleteResult.getOrDefault(0))
            }
        }
    }
}
