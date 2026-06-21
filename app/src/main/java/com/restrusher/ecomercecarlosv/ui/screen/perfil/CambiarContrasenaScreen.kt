package com.restrusher.ecomercecarlosv.ui.screen.perfil

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun CambiarContrasenaScreen(
    navController: NavController,
    viewModel: CambiarContrasenaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CambiarContrasenaContent(
        state = state,
        onBack = { navController.popBackStack() },
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSave = viewModel::onSave,
    )
}

@Composable
private fun CambiarContrasenaContent(
    state: CambiarContrasenaUiState,
    onBack: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                PedidosTopBar(
                    title = stringResource(R.string.cambiar_contrasena_title),
                    onBack = onBack,
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) {
                        Text(stringResource(R.string.common_cancelar))
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(2f).height(52.dp),
                        enabled = state.isValid && !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                if (state.isSelf) stringResource(R.string.cambiar_contrasena_actualizar)
                                else stringResource(R.string.cambiar_contrasena_guardar),
                            )
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Target user card
                PwTargetCard(state = state)

                // Scope banner (admin changing another user's password)
                if (!state.isSelf) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(ext.bananaTint)
                            .border(1.dp, ext.banana.copy(alpha = 0.22f), RoundedCornerShape(13.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_admin_panel),
                            contentDescription = null,
                            tint = ext.bananaText,
                            modifier = Modifier.size(18.dp).padding(top = 1.dp),
                        )
                        Text(
                            text = stringResource(R.string.cambiar_contrasena_scope_banner),
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.text2,
                        )
                    }
                }

                // Password fields
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.isSelf) {
                        PwField(
                            value = state.currentPassword,
                            onValueChange = onCurrentPasswordChange,
                            label = stringResource(R.string.cambiar_contrasena_actual),
                        )
                    }
                    PwField(
                        value = state.newPassword,
                        onValueChange = onNewPasswordChange,
                        label = stringResource(R.string.cambiar_contrasena_nueva),
                    )
                    PwField(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = stringResource(R.string.cambiar_contrasena_confirmar),
                        isError = state.passwordMismatch,
                        errorMessage = if (state.passwordMismatch) stringResource(R.string.cambiar_contrasena_no_coincide) else null,
                    )
                }

                // Requirements checklist
                PwRequirementsCard(state = state)

                // API error
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
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

        // Success overlay
        if (state.isSuccess) {
            SuccessOverlay(
                isSelf = state.isSelf,
                onDone = onBack,
            )
        }
    }
}

@Composable
private fun PwTargetCard(state: CambiarContrasenaUiState) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(15.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val initials = state.targetName.split(' ')
            .filter(String::isNotBlank).take(2)
            .map { it.first().uppercaseChar() }.joinToString("")
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.targetName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = state.targetEmail,
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        RoleBadge(role = state.targetRole)
    }
}

@Composable
private fun PwField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    val ext = MaterialTheme.extendedColors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = isError,
        supportingText = if (errorMessage != null) {
            { Text(errorMessage, color = ext.redText) }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = ext.border,
        ),
        singleLine = true,
    )
}

@Composable
private fun PwRequirementsCard(state: CambiarContrasenaUiState) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        RequirementRow(
            met = state.meetsLength,
            label = stringResource(R.string.cambiar_contrasena_req_longitud),
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ext.border)
        RequirementRow(
            met = state.meetsNumber,
            label = stringResource(R.string.cambiar_contrasena_req_numero),
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ext.border)
        RequirementRow(
            met = state.meetsCasing,
            label = stringResource(R.string.cambiar_contrasena_req_mayuscula),
        )
    }
}

@Composable
private fun RequirementRow(met: Boolean, label: String) {
    val ext = MaterialTheme.extendedColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = if (met) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (met) ext.greenText else ext.text4,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (met) MaterialTheme.colorScheme.onSurface else ext.text3,
        )
    }
}

@Composable
private fun SuccessOverlay(
    isSelf: Boolean,
    onDone: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.38f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(ext.greenTint),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ext.greenText,
                    modifier = Modifier.size(40.dp),
                )
            }
            Text(
                text = stringResource(R.string.cambiar_contrasena_exito_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    if (isSelf) R.string.cambiar_contrasena_exito_self
                    else R.string.cambiar_contrasena_exito_other,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = ext.text2,
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(stringResource(R.string.common_listo))
            }
        }
    }
}
