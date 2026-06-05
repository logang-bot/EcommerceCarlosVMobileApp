package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.CameraPermissionTextProvider
import com.restrusher.ecomercecarlosv.ui.common.MapsLinkField
import com.restrusher.ecomercecarlosv.ui.common.PermissionDialog
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun CreateClienteScreen(
    navController: NavController,
    viewModel: CreateClienteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateClienteContent(
        state = state,
        onBack = { navController.popBackStack() },
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onMapsUrlChange = viewModel::onMapsUrlChange,
        onPhotoSelected = viewModel::onPhotoSelected,
        onPhoneChange = viewModel::onPhoneChange,
        onAddPhone = viewModel::onAddPhone,
        onRemovePhone = viewModel::onRemovePhone,
        onSave = { viewModel.onSave { navController.popBackStack() } },
    )
}

@Composable
private fun CreateClienteContent(
    state: CreateClienteFormState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onMapsUrlChange: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onPhoneChange: (Int, String) -> Unit,
    onAddPhone: () -> Unit,
    onRemovePhone: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val context = LocalContext.current
    val activity = context as? Activity

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionPermanentlyDeclined by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) onPhotoSelected(pendingCameraUri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPhotoSelected(uri)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createClienteCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else if (activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false) {
            permissionPermanentlyDeclined = true
            showPermissionDialog = true
        }
    }

    fun handleCameraRequest() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                val uri = createClienteCameraUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            }
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true -> {
                permissionPermanentlyDeclined = false
                showPermissionDialog = true
            }
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(if (state.isEditing) R.string.create_cliente_title_edit else R.string.create_cliente_title_new),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                },
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = ext.border)
                Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Button(
                        onClick = onSave,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                    ) {
                        Text(stringResource(if (state.isEditing) R.string.create_cliente_save_edit else R.string.create_cliente_save_new), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CirclePhotoPicker(photoUri = state.photoUri, onClick = { showPhotoSheet = true })
            ClienteNameField(state = state, onNameChange = onNameChange)
            ClienteDescriptionField(state = state, onDescriptionChange = onDescriptionChange)
            PhoneListField(phones = state.phones, onPhoneChange = onPhoneChange, onAddPhone = onAddPhone, onRemovePhone = onRemovePhone)
            MapsLinkField(value = state.mapsUrl, onValueChange = onMapsUrlChange, label = stringResource(R.string.create_cliente_maps_label), required = false)
        }
    }

    if (showPhotoSheet) {
        ClientePhotoPickerSheet(
            onDismiss = { showPhotoSheet = false },
            onCamera = { showPhotoSheet = false; handleCameraRequest() },
            onGallery = { showPhotoSheet = false; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        )
    }

    if (showPermissionDialog) {
        PermissionDialog(
            permissionTextProvider = CameraPermissionTextProvider(),
            isPermanentlyDeclined = permissionPermanentlyDeclined,
            onDismiss = { showPermissionDialog = false },
            onOkClick = { showPermissionDialog = false; permissionLauncher.launch(Manifest.permission.CAMERA) },
            onGoToAppSettingsClick = {
                showPermissionDialog = false
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null) })
            },
        )
    }
}

@Composable
private fun ClienteNameField(state: CreateClienteFormState, onNameChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth()) {
        ClienteFieldLabel(text = stringResource(R.string.create_cliente_name_label), required = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.name, onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
            placeholder = { Text(stringResource(R.string.create_cliente_name_placeholder), color = ext.text3) },
            isError = state.nameError,
            supportingText = if (state.nameError) { { Text(stringResource(R.string.create_cliente_name_error), color = MaterialTheme.colorScheme.error) } } else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            colors = clienteFieldColors(),
        )
    }
}

@Composable
private fun ClienteDescriptionField(state: CreateClienteFormState, onDescriptionChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth()) {
        ClienteFieldLabel(text = stringResource(R.string.create_cliente_desc_label), required = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.description, onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), minLines = 2, maxLines = 4,
            placeholder = { Text(stringResource(R.string.create_cliente_desc_placeholder), color = ext.text3) },
            isError = state.descriptionError,
            supportingText = if (state.descriptionError) {
                { Text(stringResource(R.string.create_cliente_desc_error), color = MaterialTheme.colorScheme.error) }
            } else {
                { Text(stringResource(R.string.create_cliente_desc_hint), color = ext.text3) }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            colors = clienteFieldColors(),
        )
    }
}
