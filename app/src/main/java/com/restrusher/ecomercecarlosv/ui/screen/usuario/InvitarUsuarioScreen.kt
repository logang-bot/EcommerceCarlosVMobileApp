package com.restrusher.ecomercecarlosv.ui.screen.usuario

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import androidx.compose.ui.res.painterResource
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun InvitarUsuarioScreen(
    navController: NavController,
    viewModel: InvitarUsuarioViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InvitarUsuarioContent(
        state = state,
        onClose = { navController.popBackStack() },
        onNameChange = { viewModel.onNameChange(it) },
        onEmailChange = { viewModel.onEmailChange(it) },
        onRoleChange = { viewModel.onRoleChange(it) },
        onSend = { viewModel.onSendInvitation { navController.popBackStack() } },
    )
}

@Composable
private fun InvitarUsuarioContent(
    state: InvitarUsuarioFormState,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSend: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.invitar_usuario_title),
                onBack = onClose,
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 18.dp, vertical = 12.dp)) {
                Button(
                    onClick = onSend,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !state.isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.invitar_usuario_enviar))
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.invitar_usuario_nombre)) },
                placeholder = { Text(stringResource(R.string.invitar_usuario_nombre_hint)) },
                isError = state.nameError,
                supportingText = if (state.nameError) ({ Text(stringResource(R.string.invitar_usuario_nombre_error)) }) else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.border2,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                ),
            )

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.invitar_usuario_correo)) },
                placeholder = { Text(stringResource(R.string.invitar_usuario_correo_hint)) },
                isError = state.emailError,
                supportingText = if (state.emailError) ({
                    Text(stringResource(R.string.invitar_usuario_correo_error))
                }) else ({
                    Text(stringResource(R.string.invitar_usuario_correo_supporting))
                }),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.border2,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                ),
            )

            // Role picker
            Column {
                Text(
                    text = stringResource(R.string.invitar_usuario_rol),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text2,
                    modifier = Modifier.padding(bottom = 9.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoleOption(
                        role = UserRole.USUARIO,
                        selected = state.role == UserRole.USUARIO,
                        onClick = { onRoleChange(UserRole.USUARIO) },
                        permissions = listOf(
                            stringResource(R.string.role_perm_pedidos) to true,
                            stringResource(R.string.role_perm_no_admin) to false,
                        ),
                    )
                    RoleOption(
                        role = UserRole.SUPERUSUARIO,
                        selected = state.role == UserRole.SUPERUSUARIO,
                        onClick = { onRoleChange(UserRole.SUPERUSUARIO) },
                    )
                }

                // Superuser warning banner
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ext.bananaTint)
                        .border(1.dp, ext.banana.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_admin_panel), contentDescription = null, tint = ext.bananaText, modifier = Modifier.size(17.dp).padding(top = 1.dp))
                    Text(
                        text = stringResource(R.string.invitar_usuario_super_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.text2,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun InvitarDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        InvitarUsuarioContent(
            state = InvitarUsuarioFormState(),
            onClose = {}, onNameChange = {}, onEmailChange = {}, onRoleChange = {}, onSend = {},
        )
    }
}
