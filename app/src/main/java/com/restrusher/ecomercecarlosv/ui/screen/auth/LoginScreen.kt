package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* SyncNotifier has its own permission guard — result is not needed here */ }

    val handleLoginSuccess: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        onLoginSuccess()
    }

    val biometricCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onBiometricSuccess(handleLoginSuccess)
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

    if (state.isBiometricEnabled) {
        LoginBiometricoContent(
            state = state,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = { viewModel.onBiometricPasswordLogin(handleLoginSuccess) },
            onBiometricClick = triggerBiometric,
            onSwitchToPassword = viewModel::switchToPasswordLogin,
            onForgetUser = viewModel::onForgetUserClick,
        )
        if (state.showForgetDialog) {
            OlvidarUsuarioDialog(
                userName = state.enrolledUserName,
                onConfirm = viewModel::onForgetUserConfirm,
                onDismiss = viewModel::onForgetDialogDismiss,
            )
        }
    } else {
        LoginContent(
            state = state,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = { viewModel.onLoginClick(handleLoginSuccess) },
            onSwitchToOtherAccount = viewModel::switchToOtherAccount,
        )
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
