package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun LoginBiometricoContent(
    state: LoginFormState,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onSwitchToPassword: () -> Unit,
    onForgetUser: () -> Unit,
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
            Spacer(modifier = Modifier.height(24.dp))
            WelcomeBackCard(state)
            Spacer(modifier = Modifier.height(20.dp))

            if (state.showPasswordLogin) {
                PasswordSection(
                    state = state,
                    onPasswordChange = onPasswordChange,
                    onLoginClick = onLoginClick,
                    onBiometricClick = onBiometricClick,
                )
            } else {
                BiometricSection(
                    onBiometricClick = onBiometricClick,
                    onSwitchToPassword = onSwitchToPassword,
                )
            }

            Spacer(modifier = Modifier.weight(1.1f))
            ForgetUserRow(firstName = state.enrolledUserFirstName, onClick = onForgetUser)
        }
    }
}

@Composable
private fun ForgetUserRow(firstName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.login_no_eres, firstName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.extendedColors.text3,
        )
        Text(
            text = stringResource(R.string.login_otra_cuenta),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BiometricSection(
    onBiometricClick: () -> Unit,
    onSwitchToPassword: () -> Unit,
) {
    Button(
        onClick = onBiometricClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .padding(end = 4.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(stringResource(R.string.login_biometric))
    }
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedButton(
        onClick = onSwitchToPassword,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(stringResource(R.string.login_usar_contrasena))
    }
}

@Composable
private fun PasswordSection(
    state: LoginFormState,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onBiometricClick: () -> Unit,
) {
    LoginTextField(
        label = stringResource(R.string.login_password_label),
        value = state.password,
        onValueChange = onPasswordChange,
        isPassword = true,
        imeAction = ImeAction.Done,
        onImeAction = onLoginClick,
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
    Spacer(modifier = Modifier.height(if (state.errorMessage != null) 12.dp else 16.dp))
    PrimaryLoginButton(isLoading = state.isLoading, onClick = onLoginClick)
    // Hidden once the stored token is gone: the fingerprint would only spend a tap to arrive back
    // at this same password field.
    if (state.canUseFingerprint) {
        Spacer(modifier = Modifier.height(12.dp))
        BackToFingerprintRow(onClick = onBiometricClick)
    }
}

@Composable
private fun BackToFingerprintRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp),
        )
        Text(
            text = stringResource(R.string.login_biometric),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
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
        ProfileAvatar(initials = state.enrolledUserInitials, photoUrl = state.enrolledUserPhotoUrl, size = 68)
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

private val previewState = LoginFormState(
    isBiometricEnabled    = true,
    enrolledUserName      = "Carlos Villarroel",
    enrolledUserFirstName = "Carlos",
    enrolledUserEmail     = "carlos@comercializadora.ve",
    enrolledUserRole      = UserRole.SUPERUSUARIO,
    enrolledUserInitials  = "CV",
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Biométrico default Dark")
@Composable
private fun BiometricoDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LoginBiometricoContent(
            state = previewState,
            onPasswordChange = {}, onLoginClick = {}, onBiometricClick = {},
            onSwitchToPassword = {}, onForgetUser = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Biométrico default Light")
@Composable
private fun BiometricoLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        LoginBiometricoContent(
            state = previewState,
            onPasswordChange = {}, onLoginClick = {}, onBiometricClick = {},
            onSwitchToPassword = {}, onForgetUser = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Contraseña mode Light")
@Composable
private fun PasswordModeLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        LoginBiometricoContent(
            state = previewState.copy(showPasswordLogin = true),
            onPasswordChange = {}, onLoginClick = {}, onBiometricClick = {},
            onSwitchToPassword = {}, onForgetUser = {},
        )
    }
}

private val sessionExpiredState = previewState.copy(
    showPasswordLogin = true,
    canUseFingerprint = false,
    errorMessage = "Tu sesión expiró. Escribe tu contraseña para continuar",
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Sesión expirada Light")
@Composable
private fun SessionExpiredPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        LoginBiometricoContent(
            state = sessionExpiredState,
            onPasswordChange = {}, onLoginClick = {}, onBiometricClick = {},
            onSwitchToPassword = {}, onForgetUser = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Sesión expirada Dark")
@Composable
private fun SessionExpiredDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LoginBiometricoContent(
            state = sessionExpiredState,
            onPasswordChange = {}, onLoginClick = {}, onBiometricClick = {},
            onSwitchToPassword = {}, onForgetUser = {},
        )
    }
}
