package com.restrusher.ecomercecarlosv.ui.screen.perfil

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun EditarPerfilScreen(
    navController: NavController,
    viewModel: EditarPerfilViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EditarPerfilContent(
        state = state,
        onClose = { navController.popBackStack() },
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onSave = { viewModel.saveChanges { navController.popBackStack() } },
    )
}

@Composable
private fun EditarPerfilContent(
    state: EditarPerfilUiState,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.editar_perfil_title),
                onBack = onClose,
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.extendedColors.border)
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SaveButton(isSaving = state.isSaving, onClick = onSave)
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
            PhotoSection(initials = state.initials)
            ProfileField(
                label = stringResource(R.string.editar_perfil_nombre),
                value = state.name,
                onValueChange = onNameChange,
                keyboardType = KeyboardType.Text,
            )
            ProfileField(
                label = stringResource(R.string.editar_perfil_correo),
                value = state.email,
                onValueChange = onEmailChange,
                keyboardType = KeyboardType.Email,
            )
            ProfileField(
                label = stringResource(R.string.editar_perfil_telefono),
                value = state.phone,
                onValueChange = onPhoneChange,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
            )
            RoleReadOnlyField(role = state.role)
        }
    }
}

@Composable
private fun PhotoSection(modifier: Modifier = Modifier, initials: String) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(initials = initials, size = 104)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.editar_perfil_foto_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.extendedColors.text4,
        )
    }
}

@Composable
private fun ProfileField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
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
private fun RoleReadOnlyField(modifier: Modifier = Modifier, role: UserRole) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.editar_perfil_rol),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                RoleBadge(role = role)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.editar_perfil_rol_readonly_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.text3,
                )
            }
        }
    }
}

@Composable
private fun SaveButton(modifier: Modifier = Modifier, isSaving: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(stringResource(R.string.editar_perfil_guardar), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EditarPerfilDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        EditarPerfilContent(
            state = EditarPerfilUiState(
                name      = "Carlos Villarroel",
                email     = "carlos@comercializadora.ve",
                phone     = "0414-2230198",
                role      = UserRole.SUPERUSUARIO,
                initials  = "CV",
                isLoading = false,
            ),
            onClose = {}, onNameChange = {}, onEmailChange = {}, onPhoneChange = {}, onSave = {},
        )
    }
}
