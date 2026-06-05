package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.presentation.screens.EditarPerfilRoute
import com.restrusher.ecomercecarlosv.presentation.screens.GestionUsuariosRoute
import com.restrusher.ecomercecarlosv.presentation.screens.LoginRoute
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.common.SettingRow
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.res.painterResource
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val biometricCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onBiometricAuthSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                viewModel.onBiometricAuthFailed()
            }
            override fun onAuthenticationFailed() {
                viewModel.onBiometricAuthFailed()
            }
        }
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.perfil_biometrica_prompt_title))
            .setSubtitle(context.getString(R.string.perfil_biometrica_prompt_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .setNegativeButtonText(context.getString(R.string.common_cancelar))
            .build()
    }

    PerfilContent(
        state = state,
        onBack = { navController.popBackStack() },
        onEditProfile = { navController.navigate(EditarPerfilRoute) },
        onBiometricToggle = {
            if (state.isBiometricEnrolled) {
                viewModel.disableBiometric()
            } else {
                val activity = context.findFragmentActivity() ?: return@PerfilContent
                val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(context), biometricCallback)
                prompt.authenticate(promptInfo)
            }
        },
        onGestionUsuariosClick = { navController.navigate(GestionUsuariosRoute) },
        onLogout = {
            viewModel.logout {
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
    )
}

@Composable
private fun PerfilContent(
    state: PerfilUiState,
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onBiometricToggle: () -> Unit,
    onGestionUsuariosClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.perfil_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Identity header ────────────────────────────────────────
            Row(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                ProfileAvatar(initials = state.initials, size = 66)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier.padding(top = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        RoleBadge(role = state.role)
                        if (state.role == UserRole.SUPERUSUARIO) {
                            Text(
                                text = stringResource(R.string.perfil_super_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.text3,
                            )
                        }
                    }
                }
            }

            // ── Cuenta ────────────────────────────────────────────────
            SectionHeader(stringResource(R.string.perfil_section_cuenta))
            Column {
                SettingRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.perfil_correo),
                    subtitle = state.email,
                    onClick = onEditProfile,
                    trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 69.dp), color = ext.border)
                SettingRow(
                    icon = Icons.Default.Phone,
                    title = stringResource(R.string.perfil_telefono),
                    subtitle = state.phone.ifEmpty { null },
                    onClick = onEditProfile,
                    trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
                )
            }

            // ── Seguridad ─────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.perfil_section_seguridad))
            BiometricCard(
                available = state.isBiometricAvailable,
                enrolled = state.isBiometricEnrolled,
                enrolledDate = state.biometricEnabledDate,
                onToggle = onBiometricToggle,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(10.dp))
            SettingRow(
                icon = Icons.Default.Check,
                title = stringResource(R.string.perfil_cambiar_contrasena),
                onClick = { /* TODO: Change password flow — Phase 9 */ },
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
            )

            // ── Equipo (superuser only) ────────────────────────────────
            if (state.role == UserRole.SUPERUSUARIO) {
                Spacer(Modifier.height(14.dp))
                SectionHeader(stringResource(R.string.perfil_section_equipo))
                SettingRow(
                    icon = painterResource(R.drawable.ic_users),
                    title = stringResource(R.string.perfil_gestion_usuarios),
                    subtitle = state.teamSummary,
                    iconColor = ext.bananaText,
                    iconBg = ext.bananaTint,
                    onClick = onGestionUsuariosClick,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(painterResource(R.drawable.ic_admin_panel), contentDescription = null, tint = ext.bananaText, modifier = Modifier.size(13.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp))
                        }
                    },
                )
            }

            // ── Logout ────────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(ext.redTint)
                        .clickable(onClick = onLogout),
                ) {
                    Text(
                        text = stringResource(R.string.perfil_cerrar_sesion),
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.redText,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BiometricCard(
    available: Boolean,
    enrolled: Boolean,
    enrolledDate: String?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val cardBg = if (enrolled) ext.accentSoft else ext.surface2
    val cardBorder = if (enrolled) ext.accentTint else ext.border
    val iconBg = if (enrolled) ext.accentTint else ext.surface3
    val iconTint = if (enrolled) MaterialTheme.colorScheme.primary else ext.text2
    val subColor = when {
        !available -> ext.text4
        enrolled -> MaterialTheme.colorScheme.primary
        else -> ext.text3
    }
    val subtitleText = when {
        !available -> stringResource(R.string.perfil_biometrica_not_available)
        enrolled -> stringResource(R.string.perfil_biometrica_active)
        else -> stringResource(R.string.perfil_biometrica_hint)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (available) 1f else 0.55f)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg),
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.perfil_biometrica_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            BiometricToggle(
                on = enrolled,
                onToggle = onToggle,
                enabled = available,
            )
        }
        if (enrolled && enrolledDate != null) {
            HorizontalDivider(color = ext.border)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.perfil_biometrica_registered, enrolledDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun BiometricToggle(on: Boolean, onToggle: () -> Unit, enabled: Boolean = true) {
    val accent = MaterialTheme.colorScheme.primary
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(if (on) accent else ext.surface3)
            .border(if (on) 0.dp else 1.dp, ext.border2, CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
            .padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PerfilDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        PerfilContent(
            state = PerfilUiState(
                name = "Carlos Villarroel",
                email = "carlos@comercializadora.ve",
                role = UserRole.SUPERUSUARIO,
                initials = "CV",
                isBiometricAvailable = true,
                isBiometricEnrolled = true,
                biometricEnabledDate = "17 de marzo",
                teamSummary = "5 usuarios · 2 super usuarios",
                isLoading = false,
            ),
            onBack = {}, onBiometricToggle = {}, onGestionUsuariosClick = {}, onLogout = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PerfilLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        PerfilContent(
            state = PerfilUiState(
                name = "Daniel Ortega",
                email = "daniel@comercializadora.ve",
                role = UserRole.USUARIO,
                initials = "DO",
                isBiometricAvailable = false,
                isLoading = false,
            ),
            onBack = {}, onBiometricToggle = {}, onGestionUsuariosClick = {}, onLogout = {},
        )
    }
}
