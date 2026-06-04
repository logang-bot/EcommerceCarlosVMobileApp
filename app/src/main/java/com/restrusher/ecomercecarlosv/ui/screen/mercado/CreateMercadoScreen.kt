package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun CreateMercadoScreen(
    navController: NavController,
    viewModel: CreateMercadoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateMercadoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onNameChange = viewModel::onNameChange,
        onAddressChange = viewModel::onAddressChange,
        onSave = { viewModel.onSave { navController.popBackStack() } },
    )
}

@Composable
private fun CreateMercadoContent(
    state: CreateMercadoFormState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val isEditing = false // determined by route — ViewModel knows via mercadoId
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(if (isEditing) R.string.create_mercado_title_edit else R.string.create_mercado_title_new),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            MercadoFormFields(state, onNameChange, onAddressChange)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onSave,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                Text(stringResource(R.string.common_guardar), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun MercadoFormFields(
    state: CreateMercadoFormState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = ext.border2,
        unfocusedBorderColor = ext.border,
        cursorColor = MaterialTheme.colorScheme.primary,
    )
    val fieldShape = RoundedCornerShape(14.dp)

    Text(stringResource(R.string.create_mercado_name_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        shape = fieldShape,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.create_mercado_name_placeholder), color = ext.text3) },
        isError = state.nameError,
        supportingText = if (state.nameError) {
            { Text(stringResource(R.string.create_mercado_name_error), color = MaterialTheme.colorScheme.error) }
        } else null,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        colors = fieldColors,
    )
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.create_mercado_address_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.address,
        onValueChange = onAddressChange,
        modifier = Modifier.fillMaxWidth(),
        shape = fieldShape,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.create_mercado_address_placeholder), color = ext.text3) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        colors = fieldColors,
    )
    // TODO: Add map pin picker for precise location — Phase 2 extension
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CreateMercadoScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        CreateMercadoContent(CreateMercadoFormState(), {}, {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun CreateMercadoScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        CreateMercadoContent(CreateMercadoFormState(), {}, {}, {}, {})
    }
}
