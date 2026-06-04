package com.restrusher.ecomercecarlosv.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LoginFormState())
    val state: StateFlow<LoginFormState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            delay(300)
            val s = _state.value
            // TODO: Replace with Supabase auth — see docs/features/auth.md → Supabase Authentication
            if (s.email.trim() == "admin" && s.password == "admin") {
                _state.value = s.copy(isLoading = false)
                onSuccess()
            } else {
                _state.value = s.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
            }
        }
    }
}
