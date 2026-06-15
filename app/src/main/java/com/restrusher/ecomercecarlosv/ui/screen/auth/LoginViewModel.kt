package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.restrusher.ecomercecarlosv.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val supabase: SupabaseClient,
    private val dataSynchronizer: DataSynchronizer,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginFormState())
    val state: StateFlow<LoginFormState> = _state.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val deviceReady = BiometricManager.from(context)
                .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
            if (deviceReady && userRepository.hasBiometricEnabled()) {
                val user = userRepository.getBiometricEnabledUser()
                _state.value = _state.value.copy(
                    isBiometricEnabled = true,
                    enrolledUserName = user?.name ?: "",
                    enrolledUserEmail = user?.email ?: "",
                    enrolledUserRole = user?.role ?: UserRole.USUARIO,
                    enrolledUserInitials = computeInitials(user?.name ?: ""),
                    enrolledUserPhotoUrl = user?.photoUrl,
                )
            }
        }
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val s = _state.value
            Log.d(TAG, "onLoginClick: attempting sign-in for '${s.email.trim()}'")
            try {
                supabase.auth.signInWith(Email) {
                    email = s.email.trim()
                    password = s.password
                }
                Log.d(TAG, "onLoginClick: Supabase auth succeeded")

                val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                    Log.e(TAG, "onLoginClick: currentUserOrNull() returned null after successful sign-in")
                    _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_get_user))
                    return@launch
                }
                Log.d(TAG, "onLoginClick: userId=$userId")

                // Sync from remote first to get the freshest is_active value;
                // fall back to local cache only if the network call fails.
                val remoteUser = userRepository.syncFromRemote(userId)
                Log.d(TAG, "onLoginClick: syncFromRemote result=${if (remoteUser != null) "user(${remoteUser.name}, active=${remoteUser.isActive})" else "null"}")

                val user = remoteUser
                    ?: userRepository.getById(userId).also {
                        Log.d(TAG, "onLoginClick: local cache result=${if (it != null) "user(${it.name})" else "null"}")
                    }
                    ?: run {
                        Log.e(TAG, "onLoginClick: user not found in remote or local cache for userId=$userId")
                        _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_not_registered))
                        return@launch
                    }

                if (!user.isActive) {
                    Log.w(TAG, "onLoginClick: user is inactive, signing out")
                    supabase.auth.signOut()
                    _state.value = s.copy(isLoading = false, isAccountDisabled = true)
                    return@launch
                }

                Log.d(TAG, "onLoginClick: login successful, navigating to home")
                sessionManager.setCurrentUser(user)
                dataSynchronizer.resetStaleness()
                _state.value = s.copy(isLoading = false)
                onSuccess()
            } catch (e: RestException) {
                val body = e.message ?: ""
                Log.e(TAG, "onLoginClick: RestException — status=${e.statusCode}, error='${e.error}', description='${e.description}'")
                when {
                    body.contains("banned", ignoreCase = true) ->
                        _state.value = s.copy(isLoading = false, isAccountDisabled = true)
                    body.contains("Invalid login", ignoreCase = true) ||
                    body.contains("invalid_credentials", ignoreCase = true) ->
                        _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_invalid_credentials))
                    else ->
                        _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_auth_failed))
                }
            } catch (e: Exception) {
                Log.e(TAG, "onLoginClick: unexpected exception", e)
                _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_no_internet))
            }
        }
    }

    fun onBiometricSuccess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getBiometricEnabledUser() ?: return@launch
            sessionManager.setCurrentUser(user)
            onSuccess()
        }
    }

    fun onBiometricFailed() { /* no-op */ }

    fun onBiometricPasswordLogin(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val s = _state.value
            val enrolledUser = userRepository.getBiometricEnabledUser() ?: run {
                _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_invalid_credentials))
                return@launch
            }
            try {
                supabase.auth.signInWith(Email) {
                    email = enrolledUser.email
                    password = s.password
                }
                val user = userRepository.syncFromRemote(enrolledUser.id) ?: enrolledUser
                if (!user.isActive) {
                    supabase.auth.signOut()
                    _state.value = s.copy(isLoading = false, isAccountDisabled = true)
                    return@launch
                }
                sessionManager.setCurrentUser(user)
                dataSynchronizer.resetStaleness()
                _state.value = s.copy(isLoading = false)
                onSuccess()
            } catch (e: RestException) {
                val msg = when {
                    e.message?.contains("Invalid login") == true ||
                    e.message?.contains("invalid_credentials") == true -> str(R.string.login_error_wrong_password)
                    else -> str(R.string.login_error_auth_failed)
                }
                _state.value = s.copy(isLoading = false, errorMessage = msg)
            } catch (e: Exception) {
                Log.e(TAG, "onBiometricPasswordLogin: unexpected exception", e)
                _state.value = s.copy(isLoading = false, errorMessage = str(R.string.login_error_no_internet))
            }
        }
    }

    fun switchToPasswordLogin() {
        _state.value = _state.value.copy(showPasswordLogin = true, errorMessage = null)
    }

    fun switchToOtherAccount() {
        _state.value = _state.value.copy(
            isBiometricEnabled = false,
            showPasswordLogin = false,
            isAccountDisabled = false,
            email = "",
            password = "",
            errorMessage = null,
        )
    }

    private fun str(@StringRes id: Int) = context.getString(id)

    private fun computeInitials(name: String) = name
        .split(' ')
        .filter(String::isNotBlank)
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
