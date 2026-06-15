package com.restrusher.ecomercecarlosv.ui.screen.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun LoginContent(
    state: LoginFormState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSwitchToOtherAccount: () -> Unit = {},
) {
    val ext = MaterialTheme.extendedColors
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BrandSection()
            Spacer(modifier = Modifier.height(30.dp))

            if (state.isAccountDisabled) {
                // ── Cuenta desactivada: blocking card ─────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ext.redTint)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ext.redText.copy(alpha = 0.16f)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = ext.redText,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(Modifier.height(13.dp))
                    Text(
                        text = stringResource(R.string.login_disabled_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.login_disabled_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.text2,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: open dialer with admin number */ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.login_disabled_contact))
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSwitchToOtherAccount,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(stringResource(R.string.login_otra_cuenta))
                }
            } else {
                // ── Error banner (credenciales incorrectas) ───────────
                val hasError = state.errorMessage != null
                if (hasError) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(ext.redTint)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ext.redText,
                            modifier = Modifier.size(18.dp).padding(top = 1.dp),
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                    append(stringResource(R.string.login_error_credentials_bold))
                                }
                                withStyle(SpanStyle(color = ext.text2)) {
                                    append(stringResource(R.string.login_error_credentials_body))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Fields ─────────────────────────────────────────────
                LoginTextField(
                    label = stringResource(R.string.login_email_label),
                    value = state.email,
                    onValueChange = onEmailChange,
                    keyboardType = KeyboardType.Email,
                    isError = hasError,
                )
                Spacer(modifier = Modifier.height(12.dp))
                LoginTextField(
                    label = stringResource(R.string.login_password_label),
                    value = state.password,
                    onValueChange = onPasswordChange,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    isError = hasError,
                    errorText = if (hasError) stringResource(R.string.login_error_password_hint) else null,
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryLoginButton(isLoading = state.isLoading, onClick = onLoginClick)
            }

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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Login Light")
@Composable
private fun LoginContentLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        LoginContent(LoginFormState(), {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Login Error")
@Composable
private fun LoginErrorPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LoginContent(LoginFormState(errorMessage = "error"), {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Login Disabled")
@Composable
private fun LoginDisabledPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        LoginContent(LoginFormState(isAccountDisabled = true), {}, {}, {})
    }
}
