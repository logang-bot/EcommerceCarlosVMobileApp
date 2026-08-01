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
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import com.restrusher.ecomercecarlosv.domain.usecase.BiometricLoginResult
import com.restrusher.ecomercecarlosv.domain.usecase.BiometricLoginUseCase
import com.restrusher.ecomercecarlosv.domain.usecase.ForgetEnrolledUserUseCase
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
    private val biometricLogin: BiometricLoginUseCase,
    private val forgetEnrolledUser: ForgetEnrolledUserUseCase,
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
                userRepository.getBiometricEnabledUser()?.let { showEnrolledUser(it) }
            }
        }
    }

    private fun showEnrolledUser(user: AppUser) {
        _state.value = _state.value.copy(
            isBiometricEnabled = true,
            enrolledUserName = user.name,
            enrolledUserFirstName = user.name.substringBefore(' '),
            enrolledUserEmail = user.email,
            enrolledUserRole = user.role,
            enrolledUserInitials = computeInitials(user.name),
            enrolledUserPhotoUrl = user.photoUrl,
        )
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
            try {
                supabase.auth.signInWith(Email) {
                    email = s.email.trim()
                    password = s.password
                }
                val userId = supabase.auth.currentUserOrNull()?.id
                    ?: return@launch fail(s, R.string.login_error_get_user)
                finishPasswordLogin(userId, onSuccess)
            } catch (e: RestException) {
                Log.e(TAG, "onLoginClick: RestException — status=${e.statusCode}, error='${e.error}'")
                applyRestError(s, e)
            } catch (e: Exception) {
                Log.e(TAG, "onLoginClick: unexpected exception", e)
                fail(s, R.string.login_error_no_internet)
            }
        }
    }

    private suspend fun finishPasswordLogin(userId: String, onSuccess: () -> Unit) {
        val s = _state.value
        // Sync from remote first to get the freshest is_active value; fall back to the local cache
        // only if the network call fails.
        val user = userRepository.syncFromRemote(userId)
            ?: userRepository.getById(userId)
            ?: return fail(s, R.string.login_error_not_registered)
        if (!user.isActive) {
            supabase.auth.signOut()
            _state.value = s.copy(isLoading = false, isAccountDisabled = true)
            return
        }
        // Whoever signs in with a password owns this device from now on.
        userRepository.clearBiometricEnabledExcept(userId)
        sessionManager.setCurrentUser(user)
        dataSynchronizer.resetStaleness()
        _state.value = s.copy(isLoading = false)
        onSuccess()
    }

    fun onBiometricSuccess(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = biometricLogin()) {
                is BiometricLoginResult.Success -> completeBiometricLogin(result, onSuccess)
                BiometricLoginResult.AccountDisabled ->
                    _state.value = _state.value.copy(isLoading = false, isAccountDisabled = true)
                BiometricLoginResult.PasswordRequired -> requirePassword()
                BiometricLoginResult.NotEnrolled ->
                    _state.value = _state.value.copy(isLoading = false, isBiometricEnabled = false)
            }
        }
    }

    private fun completeBiometricLogin(result: BiometricLoginResult.Success, onSuccess: () -> Unit) {
        sessionManager.setCurrentUser(result.user)
        // Only a real session can re-download, so an offline login keeps the cached thresholds
        // instead of forcing a full refetch it cannot perform.
        if (result.isFreshSession) dataSynchronizer.resetStaleness()
        _state.value = _state.value.copy(isLoading = false)
        onSuccess()
    }

    // Reached only when Supabase rejected the stored refresh token — the account was banned,
    // deleted, or its password changed elsewhere. A password login is the only way back in.
    private fun requirePassword() {
        _state.value = _state.value.copy(
            isLoading = false,
            showPasswordLogin = true,
            errorMessage = str(R.string.login_error_session_expired),
        )
    }

    fun onBiometricFailed() { /* no-op */ }

    fun onBiometricPasswordLogin(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val s = _state.value
            val enrolledUser = userRepository.getBiometricEnabledUser()
                ?: return@launch fail(s, R.string.login_error_invalid_credentials)
            try {
                supabase.auth.signInWith(Email) {
                    email = enrolledUser.email
                    password = s.password
                }
                finishPasswordLogin(enrolledUser.id, onSuccess)
            } catch (e: RestException) {
                applyWrongPasswordError(s, e)
            } catch (e: Exception) {
                Log.e(TAG, "onBiometricPasswordLogin: unexpected exception", e)
                fail(s, R.string.login_error_no_internet)
            }
        }
    }

    fun onForgetUserClick() {
        _state.value = _state.value.copy(showForgetDialog = true)
    }

    fun onForgetDialogDismiss() {
        _state.value = _state.value.copy(showForgetDialog = false)
    }

    fun onForgetUserConfirm() {
        _state.value = _state.value.copy(showForgetDialog = false, isLoading = true)
        viewModelScope.launch {
            forgetEnrolledUser()
            _state.value = LoginFormState()
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

    private fun applyRestError(s: LoginFormState, e: RestException) {
        val body = e.message ?: ""
        when {
            body.contains("banned", ignoreCase = true) ->
                _state.value = s.copy(isLoading = false, isAccountDisabled = true)
            body.contains("Invalid login", ignoreCase = true) ||
            body.contains("invalid_credentials", ignoreCase = true) ->
                fail(s, R.string.login_error_invalid_credentials)
            else -> fail(s, R.string.login_error_auth_failed)
        }
    }

    private fun applyWrongPasswordError(s: LoginFormState, e: RestException) {
        val body = e.message ?: ""
        val invalid = body.contains("Invalid login") || body.contains("invalid_credentials")
        fail(s, if (invalid) R.string.login_error_wrong_password else R.string.login_error_auth_failed)
    }

    private fun fail(s: LoginFormState, @StringRes messageRes: Int) {
        _state.value = s.copy(isLoading = false, errorMessage = str(messageRes))
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
