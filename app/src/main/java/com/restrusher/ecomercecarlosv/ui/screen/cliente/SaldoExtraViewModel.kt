package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.usecase.CreateSaldoExtraUseCase
import com.restrusher.ecomercecarlosv.presentation.screens.SaldoExtraRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaldoExtraViewModel @Inject constructor(
    private val createSaldoExtraUseCase: CreateSaldoExtraUseCase,
    private val clienteRepository: ClienteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<SaldoExtraRoute>().clienteId
    private val _uiState = MutableStateFlow(SaldoExtraUiState())
    val uiState: StateFlow<SaldoExtraUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val name = clienteRepository.getById(clienteId)?.name.orEmpty()
            _uiState.update { it.copy(clienteName = name) }
        }
    }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value, descriptionError = false) }

    fun onAmountChange(value: String) =
        _uiState.update { it.copy(amount = value, amountError = false) }

    fun onDateChange(millis: Long) =
        _uiState.update { it.copy(date = millis) }

    fun onSave(onSuccess: () -> Unit) {
        val state = _uiState.value
        val descOk = state.description.isNotBlank()
        val amountOk = state.amount.toDoubleOrNull()?.let { it > 0 } == true
        if (!descOk || !amountOk) {
            _uiState.update { it.copy(descriptionError = !descOk, amountError = !amountOk) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                createSaldoExtraUseCase(
                    clienteId = clienteId,
                    description = state.description,
                    amount = state.amount.toDouble(),
                    date = state.date,
                )
            }
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
