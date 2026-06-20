package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.common.CameraPermissionTextProvider
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.PermissionDialog
import com.restrusher.ecomercecarlosv.ui.common.ProfileAvatar
import com.restrusher.ecomercecarlosv.ui.common.RoleBadge
import com.restrusher.ecomercecarlosv.ui.common.copyImageToCache
import com.restrusher.ecomercecarlosv.ui.common.createCameraImageUri
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        onPhotoSelected = viewModel::onPhotoSelected,
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
    onPhotoSelected: (Uri?) -> Unit,
    onSave: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionPermanentlyDeclined by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) onPhotoSelected(pendingCameraUri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val cachedUri = withContext(Dispatchers.IO) { copyImageToCache(context, uri) }
                onPhotoSelected(cachedUri ?: uri)
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context)
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
                val uri = createCameraImageUri(context)
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
                HorizontalDivider(color = ext.border)
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
            PhotoSection(
                initials = state.initials,
                photoUri = state.photoUri,
                onClick = { showPhotoSheet = true },
            )
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

    if (showPhotoSheet) {
        PerfilPhotoSheet(
            onDismiss = { showPhotoSheet = false },
            onCamera = { showPhotoSheet = false; handleCameraRequest() },
            onGallery = {
                showPhotoSheet = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
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
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    },
                )
            },
        )
    }
}

@Composable
private fun PhotoSection(modifier: Modifier = Modifier, initials: String, photoUri: Uri?, onClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ProfileAvatar(initials = initials, size = 104)
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onClick),
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.editar_perfil_foto_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.extendedColors.text4,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PerfilPhotoSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.extendedColors.surface2,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.editar_perfil_foto_hint),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = MaterialTheme.extendedColors.border)
            ListItem(
                headlineContent = { Text(stringResource(R.string.photo_sheet_take_photo)) },
                leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onCamera),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.photo_sheet_choose_gallery)) },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onGallery),
            )
        }
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
