package com.restrusher.ecomercecarlosv.ui.screen.mercado

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.CameraPermissionTextProvider
import com.restrusher.ecomercecarlosv.ui.common.MapsLinkField
import com.restrusher.ecomercecarlosv.ui.common.PermissionDialog
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.copyImageToCache
import com.restrusher.ecomercecarlosv.ui.common.createCameraImageUri
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        onMapsUrlChange = viewModel::onMapsUrlChange,
        onSave = { viewModel.onSave { navController.popBackStack() } },
        onPhotoSelected = viewModel::onPhotoSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateMercadoContent(
    state: CreateMercadoFormState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onMapsUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val context = LocalContext.current
    val activity = context as? Activity

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionPermanentlyDeclined by remember { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

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

    fun launchCamera() {
        val uri = createCameraImageUri(context)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    fun handleCameraRequest() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true -> {
                permissionPermanentlyDeclined = false
                showPermissionDialog = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(if (state.isEditing) R.string.create_mercado_title_edit else R.string.create_mercado_title_new),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = ext.border)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
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
                        Text(
                            text = stringResource(if (state.isEditing) R.string.create_mercado_save_edit else R.string.create_mercado_save_new),
                            style = MaterialTheme.typography.labelLarge,
                        )
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
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            MercadoNameField(state, onNameChange)
            MercadoAddressField(state, onAddressChange)
            MapsLinkField(
                value = state.mapsUrl,
                onValueChange = onMapsUrlChange,
                label = stringResource(R.string.create_mercado_maps_url_label),
                required = false,
            )
            MercadoPhotoPicker(
                isEditing = state.isEditing,
                photoUri = state.photoUri,
                onClick = { showPhotoSheet = true },
            )
        }
    }

    if (showPhotoSheet) {
        PhotoPickerSheet(
            onDismiss = { showPhotoSheet = false },
            onCamera = {
                showPhotoSheet = false
                handleCameraRequest()
            },
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
            onOkClick = {
                showPermissionDialog = false
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onGoToAppSettingsClick = {
                showPermissionDialog = false
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            },
        )
    }
}

@Composable
private fun MercadoNameField(state: CreateMercadoFormState, onNameChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column {
        FieldLabel(text = stringResource(R.string.create_mercado_name_label), required = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.create_mercado_name_placeholder), color = ext.text3) },
            isError = state.nameError,
            supportingText = if (state.nameError) {
                { Text(stringResource(R.string.create_mercado_name_error), color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            colors = fieldColors(),
        )
    }
}

@Composable
private fun MercadoAddressField(state: CreateMercadoFormState, onAddressChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column {
        FieldLabel(text = stringResource(R.string.create_mercado_address_label))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 2,
            maxLines = 4,
            placeholder = { Text(stringResource(R.string.create_mercado_address_placeholder), color = ext.text3) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            colors = fieldColors(),
        )
    }
}

@Composable
private fun MercadoPhotoPicker(isEditing: Boolean, photoUri: Uri?, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.create_mercado_photo_label))
                    append(" ")
                    withStyle(SpanStyle(color = ext.text3, fontWeight = FontWeight.Normal)) {
                        append(stringResource(R.string.create_mercado_photo_optional))
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(9.dp))

        val shape = RoundedCornerShape(16.dp)

        if (photoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(shape)
                    .clickable { onClick() },
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else if (isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(shape)
                    .background(ext.surface3)
                    .border(1.dp, ext.border, shape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = ext.text3,
                    modifier = Modifier.size(30.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(shape)
                    .background(ext.surface2)
                    .border(1.5.dp, ext.border2, shape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                    Text(
                        text = stringResource(R.string.create_mercado_photo_add),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ext.text2,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoPickerSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = stringResource(R.string.photo_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.photo_sheet_take_photo)) },
            leadingContent = {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
            },
            modifier = Modifier.clickable { onCamera() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.photo_sheet_choose_gallery)) },
            leadingContent = {
                Icon(Icons.Default.Image, contentDescription = null)
            },
            modifier = Modifier.clickable { onGallery() },
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun FieldLabel(text: String, required: Boolean = false) {
    if (required) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
            Text(text = "*", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.extendedColors.border2,
    unfocusedBorderColor = MaterialTheme.extendedColors.border,
    cursorColor = MaterialTheme.colorScheme.primary,
)

