package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.presentation.screens.CreateClienteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CreateClienteRoute>()
    private val mercadoId: String = route.mercadoId
    private val clienteId: String? = route.clienteId

    private val _state = MutableStateFlow(CreateClienteFormState())
    val state: StateFlow<CreateClienteFormState> = _state.asStateFlow()

    init {
        if (clienteId != null) {
            _state.value = _state.value.copy(isEditing = true)
            viewModelScope.launch {
                val c = clienteRepository.getById(clienteId) ?: return@launch
                _state.value = CreateClienteFormState(
                    name = c.name,
                    description = c.description,
                    phones = c.phones.ifEmpty { listOf("") },
                    mapsUrl = c.mapsUrl.orEmpty(),
                    isEditing = true,
                )
            }
        }
    }

    fun onNameChange(v: String) { _state.value = _state.value.copy(name = v, nameError = false) }
    fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v, descriptionError = false) }
    fun onMapsUrlChange(v: String) { _state.value = _state.value.copy(mapsUrl = v) }
    fun onPhotoSelected(uri: Uri?) { _state.value = _state.value.copy(photoUri = uri) }

    fun onPhoneChange(index: Int, value: String) {
        val phones = _state.value.phones.toMutableList().also { it[index] = value }
        _state.value = _state.value.copy(phones = phones)
    }

    fun onAddPhone() {
        _state.value = _state.value.copy(phones = _state.value.phones + "")
    }

    fun onRemovePhone(index: Int) {
        if (_state.value.phones.size <= 1) return
        _state.value = _state.value.copy(phones = _state.value.phones.filterIndexed { i, _ -> i != index })
    }

    fun onSave(onSuccess: () -> Unit) {
        val s = _state.value
        var hasError = false
        if (s.name.isBlank()) { _state.value = s.copy(nameError = true); hasError = true }
        if (s.description.isBlank()) { _state.value = _state.value.copy(descriptionError = true); hasError = true }
        if (hasError) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val phones = s.phones.map { it.trim() }.filter { it.isNotBlank() }
            clienteRepository.save(
                Cliente(
                    id = clienteId ?: UUID.randomUUID().toString(),
                    mercadoId = mercadoId,
                    name = s.name.trim(),
                    description = s.description.trim(),
                    phones = phones,
                    mapsUrl = s.mapsUrl.trim().ifBlank { null },
                    createdAt = System.currentTimeMillis(),
                ),
            )
            onSuccess()
        }
    }
}
