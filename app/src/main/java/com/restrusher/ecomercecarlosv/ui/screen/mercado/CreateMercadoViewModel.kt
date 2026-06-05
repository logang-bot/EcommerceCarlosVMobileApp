package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.util.extractMapsCoordinates
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateMercadoViewModel @Inject constructor(
    private val mercadoRepository: MercadoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mercadoId: String? = savedStateHandle.toRoute<CreateMercadoRoute>().mercadoId

    private val _state = MutableStateFlow(CreateMercadoFormState())
    val state: StateFlow<CreateMercadoFormState> = _state.asStateFlow()

    init {
        if (mercadoId != null) {
            _state.value = _state.value.copy(isEditing = true)
            viewModelScope.launch {
                val mercado = mercadoRepository.getById(mercadoId) ?: return@launch
                _state.value = CreateMercadoFormState(
                    name = mercado.name,
                    address = mercado.address,
                    mapsUrl = mercado.mapsUrl.orEmpty(),
                    isEditing = true,
                )
            }
        }
    }

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, nameError = false) }
    fun onAddressChange(value: String) { _state.value = _state.value.copy(address = value) }
    fun onMapsUrlChange(value: String) { _state.value = _state.value.copy(mapsUrl = value) }
    fun onPhotoSelected(uri: Uri?) { _state.value = _state.value.copy(photoUri = uri) }

    fun onSave(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.name.isBlank()) { _state.value = s.copy(nameError = true); return }
        val coords = extractMapsCoordinates(s.mapsUrl)
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true)
            mercadoRepository.save(
                Mercado(
                    id = mercadoId ?: UUID.randomUUID().toString(),
                    name = s.name.trim(),
                    address = s.address.trim(),
                    mapsUrl = s.mapsUrl.trim().ifBlank { null },
                    latitude = coords?.first,
                    longitude = coords?.second,
                    createdAt = System.currentTimeMillis(),
                )
            )
            onSuccess()
        }
    }
}
