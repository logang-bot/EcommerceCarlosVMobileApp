package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.mapper.UserMapper
import com.restrusher.ecomercecarlosv.data.remote.StorageService
import com.restrusher.ecomercecarlosv.di.AdminClient
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@HiltViewModel
class EditarPerfilViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    @AdminClient private val adminClient: SupabaseClient,
) : ViewModel() {

    private val _state = MutableStateFlow(EditarPerfilUiState())
    val state: StateFlow<EditarPerfilUiState> = _state.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val user = sessionManager.currentUser.value ?: return
        _state.value = EditarPerfilUiState(
            name      = user.name,
            email     = user.email,
            phone     = user.phone ?: "",
            role      = user.role,
            initials  = computeInitials(user.name),
            photoUri  = user.photoUrl?.let { Uri.parse(it) },
            isLoading = false,
        )
    }

    fun onNameChange(value: String)  { _state.value = _state.value.copy(name = value) }
    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value) }
    fun onPhoneChange(value: String) { _state.value = _state.value.copy(phone = value) }
    fun onPhotoSelected(uri: Uri?)   { _state.value = _state.value.copy(photoUri = uri) }

    fun saveChanges(onSaved: () -> Unit) {
        val user = sessionManager.currentUser.value ?: return
        val s = _state.value
        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            val newName  = s.name.trim()
            val newEmail = s.email.trim()
            val newPhone = s.phone.trim().ifEmpty { null }
            val photoUrl = s.photoUri?.let { uri ->
                val uriStr = uri.toString()
                if (uriStr.startsWith("http")) uriStr
                else runCatching { storageService.uploadPhoto("user-photos", user.id, uri) }.getOrElse { uriStr }
            }

            try {
                // Update Supabase auth user (email + metadata)
                adminClient.auth.admin.updateUserById(user.id) {
                    if (newEmail != user.email) email = newEmail
                    userMetadata = buildJsonObject {
                        put("name", newName)
                        if (newPhone != null) put("phone", newPhone)
                    }
                }
                // Upsert the updated profile into the `users` table
                val updated = user.copy(name = newName, email = newEmail, phone = newPhone, photoUrl = photoUrl)
                adminClient.from("users").upsert(UserMapper.toDto(updated))
            } catch (_: Exception) {
                // Fall through and update Room; will sync when online
            }

            userRepository.updateProfile(user.id, newName, newEmail, newPhone, photoUrl)
            val updated = user.copy(name = newName, email = newEmail, phone = newPhone, photoUrl = photoUrl)
            sessionManager.setCurrentUser(updated)
            _state.value = s.copy(isSaving = false)
            onSaved()
        }
    }

    private fun computeInitials(name: String) = name
        .split(' ')
        .filter(String::isNotBlank)
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}
