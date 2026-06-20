package com.restrusher.ecomercecarlosv.ui.screen.producto

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.CameraPermissionTextProvider
import com.restrusher.ecomercecarlosv.ui.common.PermissionDialog
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.copyImageToCache
import com.restrusher.ecomercecarlosv.ui.common.createCameraImageUri
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun CreateProductoScreen(
    navController: NavController,
    viewModel: CreateProductoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateProductoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPriceChange = viewModel::onPriceChange,
        onPhotoSelected = viewModel::onPhotoSelected,
        onSave = { viewModel.onSave { navController.popBackStack() } },
        onShowDeleteDialog = viewModel::onShowDeleteDialog,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onConfirmDelete = { viewModel.onDelete { navController.popBackStack() } },
    )
}

@Composable
private fun CreateProductoContent(
    state: CreateProductoFormState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onSave: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val context = LocalContext.current
    val activity = context as? Activity

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionPermanentlyDeclined by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
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

    var showPhotoSheet by remember { mutableStateOf(false) }

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
                title = stringResource(if (state.isEditing) R.string.create_producto_title_edit else R.string.create_producto_title_new),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = ext.border)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onSave,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            stringResource(if (state.isEditing) R.string.create_producto_save_edit else R.string.create_producto_save_new),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    if (state.isEditing) {
                        Button(
                            onClick = onShowDeleteDialog,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ext.redTint,
                                contentColor = ext.redText,
                            ),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp),
                            )
                            Text(stringResource(R.string.productos_eliminar), style = MaterialTheme.typography.labelLarge)
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SquarePhotoPicker(photoUri = state.photoUri, onClick = { showPhotoSheet = true })
            Text(
                text = stringResource(R.string.create_producto_photo_hint),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text4,
            )
            ProductoNameField(state = state, onNameChange = onNameChange)
            ProductoDescriptionField(state = state, onDescriptionChange = onDescriptionChange)
            ProductoPriceField(state = state, onPriceChange = onPriceChange)
        }
    }

    if (showPhotoSheet) {
        ProductoPhotoSheet(
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

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text(stringResource(R.string.productos_eliminar)) },
            text = { Text(stringResource(R.string.create_producto_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.common_eliminar), color = ext.redText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text(stringResource(R.string.common_cancelar))
                }
            },
        )
    }
}

@Composable
private fun SquarePhotoPicker(modifier: Modifier = Modifier, photoUri: Uri?, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(132.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ext.surface2)
            .border(1.5.dp, ext.border2, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = stringResource(R.string.create_producto_photo_placeholder),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun ProductoNameField(state: CreateProductoFormState, onNameChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth()) {
        ProductoFieldLabel(text = stringResource(R.string.create_producto_name_label), required = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.create_producto_name_placeholder), color = ext.text3) },
            isError = state.nameError,
            supportingText = if (state.nameError) {
                { Text(stringResource(R.string.create_producto_name_error), color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            colors = productoFieldColors(),
        )
    }
}

@Composable
private fun ProductoDescriptionField(state: CreateProductoFormState, onDescriptionChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth()) {
        ProductoFieldLabel(text = stringResource(R.string.create_producto_desc_label), required = false)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.create_producto_desc_placeholder), color = ext.text3) },
            supportingText = { Text(stringResource(R.string.create_producto_desc_hint), color = ext.text3) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            colors = productoFieldColors(),
        )
    }
}

@Composable
private fun ProductoPriceField(state: CreateProductoFormState, onPriceChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth()) {
        ProductoFieldLabel(text = stringResource(R.string.create_producto_price_label), required = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.price,
            onValueChange = onPriceChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            prefix = { Text("Bs.", color = if (state.priceError) MaterialTheme.colorScheme.error else ext.text2) },
            placeholder = { Text(stringResource(R.string.create_producto_price_placeholder), color = ext.text3) },
            isError = state.priceError,
            supportingText = if (state.priceError) {
                { Text(stringResource(R.string.create_producto_price_error), color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            colors = productoFieldColors(),
        )
    }
}

@Composable
private fun ProductoFieldLabel(text: String, required: Boolean) {
    val ext = MaterialTheme.extendedColors
    Row {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        if (required) {
            Text(" *", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
        } else {
            Text(
                text = "  ${stringResource(R.string.create_mercado_photo_optional)}",
                style = MaterialTheme.typography.labelMedium,
                color = ext.text3,
            )
        }
    }
}

@Composable
private fun productoFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.extendedColors.surface2,
    focusedContainerColor = MaterialTheme.extendedColors.surface2,
    unfocusedBorderColor = MaterialTheme.extendedColors.border,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ProductoPhotoSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(),
        containerColor = ext.surface2,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.create_producto_photo_placeholder),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = ext.border)
            androidx.compose.material3.ListItem(
                headlineContent = { Text(stringResource(R.string.photo_sheet_take_photo)) },
                leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onCamera),
            )
            androidx.compose.material3.ListItem(
                headlineContent = { Text(stringResource(R.string.photo_sheet_choose_gallery)) },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onGallery),
            )
        }
    }
}

