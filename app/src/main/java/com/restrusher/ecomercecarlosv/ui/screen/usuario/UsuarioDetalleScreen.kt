package com.restrusher.ecomercecarlosv.ui.screen.usuario

import android.content.res.Configuration
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.common.SettingRow
import androidx.compose.ui.res.painterResource
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun UsuarioDetalleScreen(
    navController: NavController,
    viewModel: UsuarioDetalleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UsuarioDetalleContent(
        state = state,
        onBack = { navController.popBackStack() },
        onRoleChange = { viewModel.onRoleChange(it) },
        onSaveRole = { viewModel.onSaveRole { navController.popBackStack() } },
        onDeactivate = { viewModel.onDeactivate { navController.popBackStack() } },
        onDelete = { viewModel.onDelete { navController.popBackStack() } },
    )
}

@Composable
private fun UsuarioDetalleContent(
    state: UsuarioDetalleUiState,
    onBack: () -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSaveRole: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val user = state.user

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.usuario_detalle_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { /* TODO: overflow menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        if (user == null && !state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.usuario_detalle_not_found), color = ext.text3)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (user != null) {
                // ── Identity header ───────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Text(user.initials, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = ext.text2, modifier = Modifier.padding(top = 3.dp))
                    Spacer(Modifier.height(11.dp))
                    RoleBadge(role = user.role)
                }

                Spacer(Modifier.height(16.dp))

                // ── Role selector ─────────────────────────────────────
                SectionHeader(stringResource(R.string.usuario_detalle_rol))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RoleOption(
                        role = UserRole.USUARIO,
                        selected = state.selectedRole == UserRole.USUARIO,
                        onClick = { onRoleChange(UserRole.USUARIO) },
                        permissions = listOf(
                            stringResource(R.string.role_perm_pedidos) to true,
                            stringResource(R.string.role_perm_no_admin) to false,
                        ),
                    )
                    RoleOption(
                        role = UserRole.SUPERUSUARIO,
                        selected = state.selectedRole == UserRole.SUPERUSUARIO,
                        onClick = { onRoleChange(UserRole.SUPERUSUARIO) },
                    )
                }

                if (state.selectedRole != user.role) {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Button(
                            onClick = onSaveRole,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !state.isSaving,
                        ) {
                            Text(stringResource(R.string.common_guardar))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Activity ──────────────────────────────────────────
                SectionHeader(stringResource(R.string.usuario_detalle_actividad))
                SettingRow(icon = Icons.Default.Phone, title = stringResource(R.string.usuario_detalle_ultima_sesion), subtitle = user.lastSeenLabel ?: "—")

                Spacer(Modifier.height(24.dp))

                // ── Actions ───────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDeactivate,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.redText),
                        border = BorderStroke(1.dp, ext.redText.copy(alpha = 0.4f)),
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.usuario_detalle_desactivar))
                    }
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !state.isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ext.redTint,
                            contentColor = ext.redText,
                        ),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.usuario_detalle_eliminar))
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RoleOption(
    role: UserRole,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    permissions: List<Pair<String, Boolean>>? = null,
) {
    val ext = MaterialTheme.extendedColors
    val isSuper = role == UserRole.SUPERUSUARIO
    val cardBg = if (selected) ext.accentSoft else ext.surface2
    val cardBorder = if (selected) MaterialTheme.colorScheme.primary else ext.border

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(cardBg)
            .border(if (selected) 1.5.dp else 1.dp, cardBorder, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSuper) ext.bananaTint else ext.surface3),
            ) {
                if (isSuper) {
                    Icon(
                        painter = painterResource(R.drawable.ic_admin_panel),
                        contentDescription = null,
                        tint = ext.bananaText,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = ext.text2,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSuper) stringResource(R.string.role_super) else stringResource(R.string.role_usuario),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (isSuper) stringResource(R.string.role_super_desc) else stringResource(R.string.role_usuario_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text3,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(if (selected) 0.dp else 1.6.dp, ext.border3, CircleShape),
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        if (permissions != null) {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                permissions.forEach { (label, on) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Icon(
                            imageVector = if (on) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (on) ext.greenText else ext.text4,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (on) MaterialTheme.colorScheme.onSurface else ext.text4,
                        )
                    }
                }
            }
        }
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun UsuarioDetalleDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        UsuarioDetalleContent(
            state = UsuarioDetalleUiState(
                user = UserUiModel("3", "Daniel Ortega", "daniel@cv.ve", UserRole.USUARIO, "DO", true, false, "Ayer", "Usuario"),
                selectedRole = UserRole.USUARIO,
                isLoading = false,
            ),
            onBack = {}, onRoleChange = {}, onSaveRole = {}, onDeactivate = {}, onDelete = {},
        )
    }
}
