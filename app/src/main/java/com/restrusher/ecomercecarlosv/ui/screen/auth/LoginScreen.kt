package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

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

    if (state.isBiometricEnabled && !state.showPasswordLogin) {
        LoginBiometricoContent(
            state = state,
            onBiometricClick = {
                val activity = context.findFragmentActivity() ?: return@LoginBiometricoContent
                BiometricPrompt(activity, ContextCompat.getMainExecutor(context), biometricCallback)
                    .authenticate(promptInfo)
            },
            onUsePassword = viewModel::switchToPasswordLogin,
            onOtherAccount = viewModel::switchToOtherAccount,
        )
    } else {
        LoginContent(
            state = state,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = { viewModel.onLoginClick(onLoginSuccess) },
        )
    }
}

// ── Regular login ─────────────────────────────────────────────────

@Composable
private fun LoginContent(
    state: LoginFormState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BrandSection()
            Spacer(modifier = Modifier.height(40.dp))
            LoginTextField(
                label = stringResource(R.string.login_email_label),
                value = state.email,
                onValueChange = onEmailChange,
                keyboardType = KeyboardType.Email,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginTextField(
                label = stringResource(R.string.login_password_label),
                value = state.password,
                onValueChange = onPasswordChange,
                isPassword = true,
                imeAction = ImeAction.Done,
            )
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(if (state.errorMessage != null) 12.dp else 20.dp))
            PrimaryLoginButton(isLoading = state.isLoading, onClick = onLoginClick)
            Spacer(modifier = Modifier.weight(1.3f))
            Text(
                text = stringResource(R.string.login_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.text4,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
        }
    }
}

// ── Enrolled user (biometric) login ──────────────────────────────

@Composable
private fun LoginBiometricoContent(
    state: LoginFormState,
    onBiometricClick: () -> Unit,
    onUsePassword: () -> Unit,
    onOtherAccount: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BrandSectionCompact()
            Spacer(modifier = Modifier.height(30.dp))
            WelcomeBackCard(state)
            Spacer(modifier = Modifier.height(18.dp))
            BiometricLoginButton(onClick = onBiometricClick)
            Spacer(modifier = Modifier.height(11.dp))
            OutlinedButton(
                onClick = onUsePassword,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.extendedColors.border2),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.extendedColors.text2,
                ),
            ) {
                Text(stringResource(R.string.login_usar_contrasena), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.weight(1.1f))
            Text(
                text = stringResource(R.string.login_otra_cuenta),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onOtherAccount)
                    .padding(bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun WelcomeBackCard(state: LoginFormState) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 22.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.login_bienvenido_de_nuevo),
            style = MaterialTheme.typography.labelMedium,
            color = ext.text3,
        )
        Spacer(modifier = Modifier.height(14.dp))
        ProfileAvatar(initials = state.enrolledUserInitials, size = 68)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.enrolledUserName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = state.enrolledUserEmail,
            style = MaterialTheme.typography.bodySmall,
            color = ext.text2,
            modifier = Modifier.padding(top = 3.dp),
        )
        Spacer(modifier = Modifier.height(11.dp))
        RoleBadge(role = state.enrolledUserRole)
    }
}

@Composable
private fun BiometricLoginButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(9.dp))
        Text(stringResource(R.string.login_biometric), style = MaterialTheme.typography.labelLarge)
    }
}

// ── Shared components ─────────────────────────────────────────────

@Composable
private fun BrandSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        BrandMark()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.extendedColors.text2,
        )
    }
}

@Composable
private fun BrandSectionCompact() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BrandMark(size = 64.dp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.extendedColors.text2,
        )
    }
}

@Composable
private fun BrandMark(size: Dp = 64.dp) {
    val cornerRadius = (size.value * 0.28f).dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(listOf(Color(0xFF6E9BF5), Color(0xFF4878DD)))),
    ) {
        Text(
            text = "CV",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1f).sp,
            ),
        )
    }
}

@Composable
private fun LoginTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = ext.border2,
                unfocusedBorderColor = if (value.isEmpty()) ext.border else ext.border2,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (value.isEmpty()) ext.text3 else MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

@Composable
private fun PrimaryLoginButton(modifier: Modifier = Modifier, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(stringResource(R.string.login_button), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun DividerOr() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 22.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.extendedColors.border)
        Text(
            text = stringResource(R.string.login_or),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.extendedColors.text3,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.extendedColors.border)
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Login Light")
@Composable
private fun LoginScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        LoginContent(LoginFormState(), {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Login Dark")
@Composable
private fun LoginScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LoginContent(LoginFormState(), {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Login Biometrico Dark")
@Composable
private fun LoginBiometricoPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LoginBiometricoContent(
            state = LoginFormState(
                isBiometricEnabled   = true,
                enrolledUserName     = "Carlos Villarroel",
                enrolledUserEmail    = "carlos@comercializadora.ve",
                enrolledUserRole     = UserRole.SUPERUSUARIO,
                enrolledUserInitials = "CV",
            ),
            onBiometricClick = {},
            onUsePassword = {},
            onOtherAccount = {},
        )
    }
}
