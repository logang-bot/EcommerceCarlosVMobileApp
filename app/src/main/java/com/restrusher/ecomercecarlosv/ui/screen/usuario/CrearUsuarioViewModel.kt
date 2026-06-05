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
class CrearUsuarioViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CrearUsuarioFormState())
    val state: StateFlow<CrearUsuarioFormState> = _state.asStateFlow()

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, nameError = false) }
    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, emailError = false) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, passwordError = false) }
    fun onRoleChange(role: UserRole) { _state.value = _state.value.copy(role = role) }

    fun onCreate(onDone: () -> Unit) {
        val s = _state.value
        val nameError = s.name.isBlank()
        val emailError = s.email.isBlank() || !s.email.contains('@')
        val passwordError = s.password.isBlank()
        if (nameError || emailError || passwordError) {
            _state.value = s.copy(nameError = nameError, emailError = emailError, passwordError = passwordError)
            return
        }
        _state.value = s.copy(isSending = true)
        viewModelScope.launch {
            // ─── STUB: local Room save — replace with Supabase admin call (Phase 9) ─
            // TODO (Phase 9):
            //   val result = supabaseClient.auth.admin.createUserWithEmailAndPassword(
            //       email    = s.email.trim(),
            //       password = s.password,
            //       data     = buildJsonObject {
            //           put("name", s.name.trim())
            //           put("role", s.role.name)
            //       }
            //   )
            //   Then upsert the new user into Room so local queries stay in sync:
            //     userRepository.save(result.toAppUser())
            //   Remove delay(300) once wired.
            val newUser = AppUser(
                id = UUID.randomUUID().toString(),
                email = s.email.trim(),
                name = s.name.trim(),
                role = s.role,
                isActive = true,
                createdAt = System.currentTimeMillis(),
            )
            userRepository.save(newUser)
            delay(300) // simulated network call — remove in Phase 9
            // ─────────────────────────────────────────────────────────────────────
            _state.value = s.copy(isSending = false, sent = true)
            onDone()
        }
    }
}
