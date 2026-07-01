package com.restrusher.ecomercecarlosv.ui.screen.producto

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.data.remote.StorageService
import com.restrusher.ecomercecarlosv.domain.model.Producto
import com.restrusher.ecomercecarlosv.domain.repository.ProductoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.CreateProductoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateProductoViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    private val storageService: StorageService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CreateProductoRoute>()
    private val productId: String? = route.productId

    private val _state = MutableStateFlow(CreateProductoFormState())
    val state: StateFlow<CreateProductoFormState> = _state.asStateFlow()

    init {
        if (productId != null) {
            _state.value = _state.value.copy(isEditing = true)
            viewModelScope.launch {
                val p = productoRepository.getById(productId) ?: return@launch
                _state.value = CreateProductoFormState(
                    name = p.name,
                    description = p.description.orEmpty(),
                    price = "%.2f".format(p.price),
                    photoUri = p.photoUrl?.let { Uri.parse(it) },
                    isEditing = true,
                )
            }
        }
    }

    fun onNameChange(v: String) { _state.value = _state.value.copy(name = v, nameError = false) }
    fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onPriceChange(v: String) { _state.value = _state.value.copy(price = v, priceError = false) }
    fun onPhotoSelected(uri: Uri?) { _state.value = _state.value.copy(photoUri = uri) }

    fun onShowDeleteDialog() { _state.value = _state.value.copy(showDeleteDialog = true) }
    fun onDismissDeleteDialog() { _state.value = _state.value.copy(showDeleteDialog = false) }

    fun onSave(onSuccess: () -> Unit) {
        val s = _state.value
        var hasError = false
        if (s.name.isBlank()) {
            _state.value = s.copy(nameError = true)
            hasError = true
        }
        val price = s.price.replace(",", ".").toDoubleOrNull()
        if (price == null || price <= 0.0) {
            _state.value = _state.value.copy(priceError = true)
            hasError = true
        }
        if (hasError) return

        val id = productId ?: UUID.randomUUID().toString()
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val photoUrl = resolvePhotoUrl(s.photoUri, bucket = "producto-photos", entityId = id)
            productoRepository.save(
                Producto(
                    id = id,
                    name = s.name.trim(),
                    description = s.description.trim().ifBlank { null },
                    price = price!!,
                    photoUrl = photoUrl,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            onSuccess()
        }
    }

    private suspend fun resolvePhotoUrl(uri: Uri?, bucket: String, entityId: String): String? {
        uri ?: return null
        val uriStr = uri.toString()
        if (uriStr.startsWith("http")) return uriStr
        return runCatching { storageService.uploadPhoto(bucket, entityId, uri) }
            .onFailure { Log.e(TAG, "Photo upload failed [$bucket/$entityId]: ${it.javaClass.simpleName}: ${it.message}") }
            .getOrElse { uriStr } // offline: keep local URI so image is visible; QueueProcessor will upload later
    }

    companion object { private const val TAG = "CreateProductoVM" }

    fun onDelete(onSuccess: () -> Unit) {
        val id = productId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, showDeleteDialog = false)
            productoRepository.delete(id)
            onSuccess()
        }
    }
}
