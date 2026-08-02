package com.restrusher.ecomercecarlosv.ui.screen.usuario

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun CrearUsuarioScreen(
    navController: NavController,
    viewModel: CrearUsuarioViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CrearUsuarioContent(
        state = state,
        onClose = { navController.popBackStack() },
        onNameChange = { viewModel.onNameChange(it) },
        onEmailChange = { viewModel.onEmailChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onRoleChange = { viewModel.onRoleChange(it) },
        onSend = { viewModel.onCreate { navController.popBackStack() } },
    )
}

@Composable
private fun CrearUsuarioContent(
    state: CrearUsuarioFormState,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSend: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.crear_usuario_title),
                onBack = onClose,
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = onSend,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !state.isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.crear_usuario_cta))
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
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.crear_usuario_nombre)) },
                placeholder = { Text(stringResource(R.string.crear_usuario_nombre_hint)) },
                isError = state.nameError,
                supportingText = if (state.nameError) ({ Text(stringResource(R.string.crear_usuario_nombre_error)) }) else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.border2,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                ),
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.crear_usuario_correo)) },
                placeholder = { Text(stringResource(R.string.crear_usuario_correo_hint)) },
                isError = state.emailError,
                supportingText = if (state.emailError) ({ Text(stringResource(R.string.crear_usuario_correo_error)) }) else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.border2,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                ),
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.crear_usuario_contrasena)) },
                isError = state.passwordError,
                supportingText = if (state.passwordError) ({
                    Text(stringResource(R.string.crear_usuario_contrasena_error))
                }) else ({
                    Text(stringResource(R.string.crear_usuario_contrasena_hint))
                }),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.border2,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                ),
            )

            Column {
                Text(
                    text = stringResource(R.string.crear_usuario_rol),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text2,
                    modifier = Modifier.padding(bottom = 9.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoleOption(
                        role = UserRole.SUPERUSUARIO,
                        selected = state.role == UserRole.SUPERUSUARIO,
                        onClick = { onRoleChange(UserRole.SUPERUSUARIO) },
                    )
                    RoleOption(
                        role = UserRole.USUARIO,
                        selected = state.role == UserRole.USUARIO,
                        onClick = { onRoleChange(UserRole.USUARIO) },
                        permissions = if (state.role == UserRole.USUARIO) listOf(
                            stringResource(R.string.role_perm_pedidos) to true,
                            stringResource(R.string.role_perm_no_admin) to false,
                        ) else null,
                    )
                    RoleOption(
                        role = UserRole.INVITADO,
                        selected = state.role == UserRole.INVITADO,
                        onClick = { onRoleChange(UserRole.INVITADO) },
                        permissions = if (state.role == UserRole.INVITADO) listOf(
                            stringResource(R.string.role_perm_ver_todo) to true,
                            stringResource(R.string.role_perm_sin_edicion) to false,
                        ) else null,
                    )
                }
            }

            // API error
            val apiError = state.errorMessage ?: state.errorRes?.let { stringResource(it) }
            if (apiError != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = apiError,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.redText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ext.redTint)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CrearDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        CrearUsuarioContent(
            state = CrearUsuarioFormState(),
            onClose = {}, onNameChange = {}, onEmailChange = {}, onPasswordChange = {}, onRoleChange = {}, onSend = {},
        )
    }
}
