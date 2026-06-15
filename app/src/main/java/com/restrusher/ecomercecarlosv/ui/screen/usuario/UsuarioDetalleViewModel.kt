package com.restrusher.ecomercecarlosv.ui.screen.usuario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.data.mapper.UserMapper
import com.restrusher.ecomercecarlosv.di.AdminClient
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.UsuarioDetalleRoute
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
class UsuarioDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    @AdminClient private val adminClient: SupabaseClient,
) : ViewModel() {

    private val userId: String = savedStateHandle.toRoute<UsuarioDetalleRoute>().userId

    private val _state = MutableStateFlow(UsuarioDetalleUiState())
    val state: StateFlow<UsuarioDetalleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getById(userId)
            val currentId = sessionManager.currentUser.value?.id
            if (user != null) {
                _state.value = UsuarioDetalleUiState(
                    user         = user.toUiModel(currentId),
                    selectedRole = user.role,
                    isLoading    = false,
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun onRoleChange(role: UserRole) {
        _state.value = _state.value.copy(selectedRole = role)
    }

    fun onSaveRole(onDone: () -> Unit) {
        val uiUser = _state.value.user ?: return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            val domainUser = userRepository.getById(uiUser.id) ?: run {
                _state.value = _state.value.copy(isSaving = false)
                return@launch
            }
            val newRole = _state.value.selectedRole
            try {
                adminClient.auth.admin.updateUserById(userId) {
                    userMetadata = buildJsonObject { put("role", newRole.name) }
                }
                adminClient.from("users").upsert(UserMapper.toDto(domainUser.copy(role = newRole)))
            } catch (_: Exception) { /* will sync later */ }
            userRepository.save(domainUser.copy(role = newRole))
            _state.value = _state.value.copy(isSaving = false)
            onDone()
        }
    }

    fun onDeactivate(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                // Supabase Auth has no permanent-ban flag; "876000h" (~100 years) is the maximum
                // supported duration and serves as an indefinite disable.
                adminClient.auth.admin.updateUserById(userId) { banDuration = "876000h" }
                val domainUser = userRepository.getById(userId)
                if (domainUser != null) {
                    adminClient.from("users").upsert(UserMapper.toDto(domainUser.copy(isActive = false)))
                }
            } catch (_: Exception) { /* will sync later */ }
            userRepository.setActive(userId, false)
            onDone()
        }
    }

    fun onActivate(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                adminClient.auth.admin.updateUserById(userId) { banDuration = "none" }
                val domainUser = userRepository.getById(userId)
                if (domainUser != null) {
                    adminClient.from("users").upsert(UserMapper.toDto(domainUser.copy(isActive = true)))
                }
            } catch (_: Exception) { /* will sync later */ }
            userRepository.setActive(userId, true)
            onDone()
        }
    }

    fun onDelete(onDone: () -> Unit) {
        _state.value = _state.value.copy(isDeleting = true)
        viewModelScope.launch {
            try {
                adminClient.auth.admin.deleteUser(userId)
                adminClient.from("users").delete { filter { eq("id", userId) } }
            } catch (_: Exception) { /* will sync later */ }
            userRepository.delete(userId)
            _state.value = _state.value.copy(isDeleting = false)
            onDone()
        }
    }
}
