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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.restrusher.ecomercecarlosv.presentation.screens.CrearUsuarioRoute
import com.restrusher.ecomercecarlosv.presentation.screens.UsuarioDetalleRoute
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import androidx.compose.ui.res.painterResource
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun GestionUsuariosScreen(
    navController: NavController,
    viewModel: GestionUsuariosViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GestionUsuariosContent(
        state = state,
        onBack = { navController.popBackStack() },
        onUserClick = { navController.navigate(UsuarioDetalleRoute(it)) },
        onInviteClick = { navController.navigate(CrearUsuarioRoute) },
    )
}

@Composable
private fun GestionUsuariosContent(
    state: GestionUsuariosUiState,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onInviteClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.gestion_usuarios_title),
                subtitle = if (!state.isLoading) state.summary else null,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { /* TODO: search users */ }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.gestion_usuarios_crear)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onInviteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            item {
                // Superuser scope banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(ext.bananaTint)
                        .border(1.dp, ext.banana.copy(alpha = 0.22f), RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_admin_panel), contentDescription = null, tint = ext.bananaText, modifier = Modifier.size(19.dp).padding(top = 1.dp))
                    val scopePre = stringResource(R.string.gestion_scope_pre)
                    val scopeBold = stringResource(R.string.gestion_scope_bold)
                    val scopePost = stringResource(R.string.gestion_scope_post)
                    Text(
                        text = buildAnnotatedString {
                            append(scopePre)
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(scopeBold) }
                            append(scopePost)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.text2,
                    )
                }
            }

            if (state.superUsuarios.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.gestion_usuarios_section_super, state.superUsuarios.size))
                }
                items(state.superUsuarios, key = { it.id }) { user ->
                    UserRow(user = user, onClick = { onUserClick(user.id) })
                    HorizontalDivider(modifier = Modifier.padding(start = 79.dp), color = ext.border)
                }
            }

            if (state.usuarios.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(18.dp))
                    SectionLabel(stringResource(R.string.gestion_usuarios_section_user, state.usuarios.size))
                }
                items(state.usuarios, key = { it.id }) { user ->
                    UserRow(user = user, onClick = { onUserClick(user.id) })
                    HorizontalDivider(modifier = Modifier.padding(start = 79.dp), color = ext.border)
                }
            }

            if (state.invitados.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(18.dp))
                    SectionLabel(stringResource(R.string.gestion_usuarios_section_invitado, state.invitados.size))
                }
                items(state.invitados, key = { it.id }) { user ->
                    UserRow(user = user, onClick = { onUserClick(user.id) })
                    HorizontalDivider(modifier = Modifier.padding(start = 79.dp), color = ext.border)
                }
            }

            item { Spacer(Modifier.height(96.dp)) } // FAB clearance
        }
    }
}

@Composable
fun UserRow(
    user: UserUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (user.isActive) 1f else 0.55f)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        // Avatar with initials
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Text(
                text = user.initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                if (user.isCurrentUser) {
                    Text(
                        text = stringResource(R.string.gestion_usuarios_me),
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.text3,
                    )
                }
            }
            val lastSeenLabel = user.lastSeenLabel
            Text(
                text = if (user.isActive) {
                    if (lastSeenLabel != null)
                        stringResource(R.string.gestion_usuarios_activo_sesion, lastSeenLabel)
                    else
                        stringResource(R.string.gestion_usuarios_activo)
                } else {
                    stringResource(R.string.gestion_usuarios_desactivado)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                modifier = Modifier.padding(top = 3.dp),
                maxLines = 1,
            )
        }
        RoleBadge(role = user.role, small = true)
    }
}

@Composable
private fun SectionLabel(title: String) {
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
private fun GestionDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        GestionUsuariosContent(
            state = GestionUsuariosUiState(
                superUsuarios = listOf(
                    UserUiModel("1", "Carlos Villarroel", "carlos@cv.ve", UserRole.SUPERUSUARIO, "CV", true, true, "Ahora"),
                    UserUiModel("2", "Rosa Villarroel", "rosa@cv.ve", UserRole.SUPERUSUARIO, "RV", true, false, "Hace 2 h"),
                ),
                usuarios = listOf(
                    UserUiModel("3", "Daniel Ortega", "daniel@cv.ve", UserRole.USUARIO, "DO", true, false, "Ayer"),
                    UserUiModel("4", "Keila Bravo", "keila@cv.ve", UserRole.USUARIO, "KB", false, false, null),
                ),
                invitados = listOf(
                    UserUiModel("5", "Néstor Lugo", "nestor@cv.ve", UserRole.INVITADO, "NL", true, false, "Hace 5 h"),
                ),
                isLoading = false,
            ),
            onBack = {}, onUserClick = {}, onInviteClick = {},
        )
    }
}
