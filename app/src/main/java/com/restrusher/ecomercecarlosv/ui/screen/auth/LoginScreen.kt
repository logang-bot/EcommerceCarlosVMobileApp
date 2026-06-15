package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val biometricCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onBiometricSuccess(onLoginSuccess)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                viewModel.onBiometricFailed()
            }
            override fun onAuthenticationFailed() {
                viewModel.onBiometricFailed()
            }
        }
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.login_biometric_prompt_title))
            .setSubtitle(context.getString(R.string.login_biometric_prompt_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .setNegativeButtonText(context.getString(R.string.common_cancelar))
            .build()
    }

    val triggerBiometric = {
        val activity = context.findFragmentActivity()
        if (activity != null) {
            BiometricPrompt(activity, ContextCompat.getMainExecutor(context), biometricCallback)
                .authenticate(promptInfo)
        }
    }

    LoadingOverlay(isLoading = state.isLoading) {
        if (state.isBiometricEnabled) {
            LoginBiometricoContent(
                state = state,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = { viewModel.onBiometricPasswordLogin(onLoginSuccess) },
                onBiometricClick = triggerBiometric,
                onSwitchToPassword = viewModel::switchToPasswordLogin,
                onOtherAccount = viewModel::switchToOtherAccount,
            )
        } else {
            LoginContent(
                state = state,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = { viewModel.onLoginClick(onLoginSuccess) },
                onSwitchToOtherAccount = viewModel::switchToOtherAccount,
            )
        }
    }
}

internal fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
