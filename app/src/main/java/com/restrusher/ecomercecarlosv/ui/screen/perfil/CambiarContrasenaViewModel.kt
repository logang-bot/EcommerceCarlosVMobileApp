package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.data.remote.AdminOperationException
import com.restrusher.ecomercecarlosv.data.remote.AdminUserService
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.presentation.screens.CambiarContrasenaRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CambiarContrasenaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val supabase: SupabaseClient,
    private val adminUserService: AdminUserService,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CambiarContrasenaRoute>()
    private val userId: String = route.userId
    private val isSelf: Boolean = route.isSelf

    private companion object {
        const val TAG = "CambiarContrasenaVM"
    }

    private val _state = MutableStateFlow(CambiarContrasenaUiState(isSelf = isSelf))
    val state: StateFlow<CambiarContrasenaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = if (isSelf) {
                sessionManager.currentUser.value
            } else {
                userRepository.getById(userId)
            }
            if (user != null) {
                _state.value = _state.value.copy(
                    targetName  = user.name,
                    targetEmail = user.email,
                    targetRole  = user.role,
                )
            }
        }
    }

    fun onCurrentPasswordChange(value: String) {
        _state.value = _state.value.copy(currentPassword = value, errorMessage = null, errorRes = null)
    }

    fun onNewPasswordChange(value: String) {
        _state.value = _state.value.copy(newPassword = value, errorMessage = null, errorRes = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, errorMessage = null, errorRes = null)
    }

    fun onSave() {
        val s = _state.value
        if (!s.isValid) return
        _state.value = s.copy(isLoading = true, errorMessage = null, errorRes = null)
        viewModelScope.launch {
            try {
                if (isSelf) {
                    supabase.auth.signInWith(Email) {
                        email = s.targetEmail
                        password = s.currentPassword
                    }
                    supabase.auth.updateUser { password = s.newPassword }
                } else {
                    // Resetting another user's password requires the service role — done
                    // server-side by the Edge Function, which verifies the caller is SUPERUSUARIO.
                    adminUserService.resetPassword(userId, s.newPassword)
                }
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } catch (e: AdminOperationException) {
                // Only the Edge Function's own Spanish text may be shown; anything else falls back.
                fail(e.serverMessage, R.string.cambiar_contrasena_error_generico)
            } catch (e: AuthRestException) {
                // Self-service branch. e.message embeds the request headers — read the code instead.
                Log.w(TAG, "onSave: auth rejected — code='${e.errorCode}'", e)
                val res = if (e.errorCode == AuthErrorCode.InvalidCredentials) {
                    R.string.login_error_wrong_password
                } else {
                    R.string.cambiar_contrasena_error_generico
                }
                fail(errorRes = res)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "onSave: unexpected exception", e)
                fail(errorRes = R.string.cambiar_contrasena_error_generico)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null, errorRes = null)
    }

    private fun fail(serverMessage: String? = null, @StringRes errorRes: Int) {
        _state.value = _state.value.copy(
            isLoading    = false,
            errorMessage = serverMessage,
            errorRes     = errorRes,
        )
    }
}
