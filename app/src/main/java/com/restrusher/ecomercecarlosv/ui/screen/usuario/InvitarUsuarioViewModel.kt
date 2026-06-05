package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvitarUsuarioViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InvitarUsuarioFormState())
    val state: StateFlow<InvitarUsuarioFormState> = _state.asStateFlow()

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, nameError = false) }
    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, emailError = false) }
    fun onRoleChange(role: UserRole) { _state.value = _state.value.copy(role = role) }

    fun onSendInvitation(onDone: () -> Unit) {
        val s = _state.value
        val nameError = s.name.isBlank()
        val emailError = s.email.isBlank() || !s.email.contains('@')
        if (nameError || emailError) {
            _state.value = s.copy(nameError = nameError, emailError = emailError)
            return
        }
        _state.value = s.copy(isSending = true)
        viewModelScope.launch {
            // TODO: Replace with Supabase invite user API — see docs/features/usuarios.md → Supabase Integration
            val newUser = AppUser(
                id = UUID.randomUUID().toString(),
                email = s.email.trim(),
                name = s.name.trim(),
                role = s.role,
                isActive = true,
                createdAt = System.currentTimeMillis(),
            )
            userRepository.save(newUser)
            delay(300) // simulated network call
            _state.value = s.copy(isSending = false, sent = true)
            onDone()
        }
    }
}
