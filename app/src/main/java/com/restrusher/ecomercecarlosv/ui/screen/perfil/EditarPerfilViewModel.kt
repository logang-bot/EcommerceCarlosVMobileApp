package com.restrusher.ecomercecarlosv.ui.screen.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditarPerfilViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditarPerfilUiState())
    val state: StateFlow<EditarPerfilUiState> = _state.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val user = sessionManager.currentUser.value ?: return
        _state.value = EditarPerfilUiState(
            name = user.name,
            email = user.email,
            phone = user.phone ?: "",
            role = user.role,
            initials = computeInitials(user.name),
            isLoading = false,
        )
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value)
    }

    fun saveChanges(onSaved: () -> Unit) {
        val user = sessionManager.currentUser.value ?: return
        val s = _state.value
        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            userRepository.updateProfile(user.id, s.name.trim(), s.email.trim(), s.phone.trim().ifEmpty { null })
            // TODO (Phase 9): Sync profile changes to Supabase before updating Room:
            //   supabaseClient.auth.admin.updateUserById(user.id) {
            //       if (s.email.trim() != user.email) email = s.email.trim()
            //       userMetadata = buildJsonObject {
            //           put("name",  s.name.trim())
            //           put("phone", s.phone.trim().ifEmpty { null })
            //       }
            //   }
            //   Note: email changes require re-verification in Supabase by default.
            val updated = user.copy(name = s.name.trim(), email = s.email.trim(), phone = s.phone.trim().ifEmpty { null })
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
