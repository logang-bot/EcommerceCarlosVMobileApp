package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.remote.AdminUserService
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrearUsuarioViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val adminUserService: AdminUserService,
) : ViewModel() {

    private val _state = MutableStateFlow(CrearUsuarioFormState())
    val state: StateFlow<CrearUsuarioFormState> = _state.asStateFlow()

    fun onNameChange(value: String)     { _state.value = _state.value.copy(name = value, nameError = false) }
    fun onEmailChange(value: String)    { _state.value = _state.value.copy(email = value, emailError = false) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, passwordError = false) }
    fun onRoleChange(role: UserRole)    { _state.value = _state.value.copy(role = role) }

    fun onCreate(onDone: () -> Unit) {
        val s = _state.value
        val nameError     = s.name.isBlank()
        val emailError    = s.email.isBlank() || !s.email.contains('@')
        val passwordError = s.password.isBlank()
        if (nameError || emailError || passwordError) {
            _state.value = s.copy(nameError = nameError, emailError = emailError, passwordError = passwordError)
            return
        }
        _state.value = s.copy(isSending = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // 1. Create the Supabase Auth user + `users` row server-side (Edge Function).
                //    The service-role secret stays on the server; this call carries the
                //    current SUPERUSUARIO's JWT, which the function verifies.
                val created = adminUserService.createUser(
                    email    = s.email.trim(),
                    password = s.password,
                    name     = s.name.trim(),
                    role     = s.role.name,
                )

                // 2. Cache locally in Room so the user appears immediately
                val appUser = AppUser(
                    id        = created.id,
                    email     = created.email,
                    name      = created.name,
                    role      = s.role,
                    isActive  = created.isActive,
                    createdAt = created.createdAt,
                )
                userRepository.save(appUser)

                _state.value = s.copy(isSending = false, sent = true)
                onDone()
            } catch (e: Exception) {
                // The Edge Function already returns Spanish messages (e.g. the duplicate-email
                // case → "Ya existe un usuario con ese correo"); surface them directly.
                val msg = e.message?.takeIf { it.isNotBlank() } ?: "Error al crear usuario"
                _state.value = s.copy(isSending = false, errorMessage = msg)
            }
        }
    }
}
