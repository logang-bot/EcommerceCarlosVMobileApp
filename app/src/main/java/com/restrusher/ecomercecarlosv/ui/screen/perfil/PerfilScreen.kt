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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.restrusher.ecomercecarlosv.domain.model.ThemeMode
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.presentation.screens.EditarPerfilRoute
import com.restrusher.ecomercecarlosv.presentation.screens.GestionUsuariosRoute
import com.restrusher.ecomercecarlosv.presentation.screens.LoginRoute
import com.restrusher.ecomercecarlosv.presentation.screens.UmbralesRoute
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.common.SettingRow
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
        onUmbralesClick = { navController.navigate(UmbralesRoute) },
        onThemeChange = { viewModel.setTheme(it) },
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
    onUmbralesClick: () -> Unit = {},
    onThemeChange: (ThemeMode) -> Unit = {},
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
            // Identity header
            Row(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                ProfileAvatar(initials = state.initials, photoUrl = state.photoUrl, size = 66)
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

            // Cuenta
            SectionHeader(stringResource(R.string.perfil_section_cuenta))
            Column {
                SettingRow(
                    icon = Icons.Default.Email,
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

            // Seguridad
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
                icon = Icons.Default.Lock,
                title = stringResource(R.string.perfil_cambiar_contrasena),
                onClick = { /* TODO: Change password flow — Phase 9 */ },
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
            )

            // Equipo (superuser only)
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

            // Ajustes (all users)
            Spacer(Modifier.height(14.dp))
            SectionHeader(stringResource(R.string.perfil_section_ajustes))
            AppearanceCard(
                themeMode = state.themeMode,
                onSelect = onThemeChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (state.role == UserRole.SUPERUSUARIO) {
                Spacer(Modifier.height(10.dp))
                SettingRow(
                    icon = Icons.Default.BarChart,
                    title = stringResource(R.string.perfil_umbrales_title),
                    subtitle = state.umbralesSummary,
                    onClick = onUmbralesClick,
                    trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
                )
            }

            // Logout
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
private fun AppearanceCard(
    themeMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ext.border, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(ext.surface2)
            .padding(bottom = 12.dp),
    ) {
        // Header row
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ext.surface3),
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = ext.text2,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.perfil_apariencia_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.perfil_apariencia_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text3,
                )
            }
        }

        // Segment selector
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(ext.surface3)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ThemeOption(
                icon = Icons.Default.WbSunny,
                label = stringResource(R.string.perfil_theme_light),
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { onSelect(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f),
            )
            ThemeOption(
                icon = Icons.Default.DarkMode,
                label = stringResource(R.string.perfil_theme_dark),
                selected = themeMode == ThemeMode.DARK,
                onClick = { onSelect(ThemeMode.DARK) },
                modifier = Modifier.weight(1f),
            )
            ThemeOption(
                icon = Icons.Default.SettingsBrightness,
                label = stringResource(R.string.perfil_theme_system),
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { onSelect(ThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ext = MaterialTheme.extendedColors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) onAccent else ext.text2,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) onAccent else ext.text2,
            fontSize = 12.5.sp,
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
    EcomerceCarlosVTheme(themeMode = ThemeMode.DARK) {
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
                themeMode = ThemeMode.DARK,
                isLoading = false,
            ),
            onBack = {}, onBiometricToggle = {}, onGestionUsuariosClick = {}, onLogout = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PerfilLightPreview() {
    EcomerceCarlosVTheme(themeMode = ThemeMode.LIGHT) {
        PerfilContent(
            state = PerfilUiState(
                name = "Daniel Ortega",
                email = "daniel@comercializadora.ve",
                role = UserRole.USUARIO,
                initials = "DO",
                isBiometricAvailable = false,
                themeMode = ThemeMode.SYSTEM,
                isLoading = false,
            ),
            onBack = {}, onBiometricToggle = {}, onGestionUsuariosClick = {}, onLogout = {},
        )
    }
}
