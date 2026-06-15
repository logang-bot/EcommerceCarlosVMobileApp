package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.remote.dto.UserDto
import com.restrusher.ecomercecarlosv.di.AdminClient
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put
import javax.inject.Inject

@HiltViewModel
class CrearUsuarioViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @AdminClient private val adminClient: SupabaseClient,
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
                // 1. Create Supabase Auth user via the admin API
                val authUser = adminClient.auth.admin.createUserWithEmail {
                    email       = s.email.trim()
                    password    = s.password
                    autoConfirm = true
                    userMetadata {
                        put("name", s.name.trim())
                        put("role", s.role.name)
                    }
                }

                // 2. Insert the user's profile row into the `users` table
                val now = System.currentTimeMillis()
                val dto = UserDto(
                    id        = authUser.id,
                    email     = s.email.trim(),
                    name      = s.name.trim(),
                    role      = s.role.name,
                    isActive  = true,
                    createdAt = now,
                )
                adminClient.from("users").insert(dto)

                // 3. Cache locally in Room so the user appears immediately
                val appUser = AppUser(
                    id        = authUser.id,
                    email     = s.email.trim(),
                    name      = s.name.trim(),
                    role      = s.role,
                    isActive  = true,
                    createdAt = now,
                )
                userRepository.save(appUser)

                _state.value = s.copy(isSending = false, sent = true)
                onDone()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("already registered") == true ||
                    e.message?.contains("already exists") == true -> "Ya existe un usuario con ese correo"
                    else -> "Error al crear usuario: ${e.message}"
                }
                _state.value = s.copy(isSending = false, errorMessage = msg)
            }
        }
    }
}
