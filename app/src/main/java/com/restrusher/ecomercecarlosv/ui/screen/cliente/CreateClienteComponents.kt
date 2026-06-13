package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun CirclePhotoPicker(
    modifier: Modifier = Modifier,
    photoUri: Uri?,
    name: String = "",
    isEditing: Boolean = false,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val context = LocalContext.current
    var bitmap by remember(photoUri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(photoUri) {
        bitmap = if (photoUri != null) {
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(photoUri)?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } catch (e: Exception) { null }
            }
        } else null
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(ext.surface2)
                .border(1.5.dp, ext.border2, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (isEditing) {
                ClienteAvatar(name = name, size = 96.dp)
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ext.text3,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp, end = 4.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun PhoneListField(
    modifier: Modifier = Modifier,
    phones: List<String>,
    primaryPhoneIndex: Int,
    isEditing: Boolean,
    onPhoneChange: (Int, String) -> Unit,
    onAddPhone: () -> Unit,
    onRemovePhone: (Int) -> Unit,
    onSetPrimaryPhone: (Int) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val primary = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ClienteFieldLabel(text = stringResource(R.string.create_cliente_phones_label))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = ext.text4,
                modifier = Modifier.size(13.dp).padding(top = 1.dp),
            )
            Text(
                text = buildAnnotatedString {
                    append("El teléfono ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = ext.text2)) { append("principal") }
                    append(" es el que aparece en el detalle del cliente.")
                },
                fontSize = 11.5.sp,
                color = ext.text3,
                lineHeight = 16.sp,
            )
        }
        phones.forEachIndexed { index, phone ->
            PhoneRow(
                phone = phone,
                isPrimary = index == primaryPhoneIndex,
                isOnlyOne = phones.size == 1,
                isEditing = isEditing,
                onValueChange = { onPhoneChange(index, it) },
                onSetPrimary = { onSetPrimaryPhone(index) },
                onRemove = { onRemovePhone(index) },
                onCall = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                    }
                },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, ext.border2, RoundedCornerShape(14.dp))
                .clickable(onClick = onAddPhone)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.create_cliente_add_phone),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary,
            )
        }
    }
}

@Composable
private fun PhoneRow(
    phone: String,
    isPrimary: Boolean,
    isOnlyOne: Boolean,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onSetPrimary: () -> Unit,
    onRemove: () -> Unit,
    onCall: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(ext.surface2)
            .border(if (isPrimary) 1.5.dp else 1.dp, if (isPrimary) primary else ext.border2, shape)
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .then(
                    if (isPrimary) Modifier.background(primary)
                    else Modifier.border(1.7.dp, ext.text3, CircleShape),
                )
                .clickable(onClick = onSetPrimary),
            contentAlignment = Alignment.Center,
        ) {
            if (isPrimary) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Icon(Icons.Default.Phone, contentDescription = null, tint = ext.text3, modifier = Modifier.size(16.dp))
        BasicTextField(
            value = phone,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            decorationBox = { innerTextField ->
                Box {
                    if (phone.isEmpty()) {
                        Text(
                            text = stringResource(R.string.create_cliente_phone_placeholder),
                            fontSize = 15.sp,
                            color = ext.text4,
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (isPrimary) {
            Text(
                text = "PRINCIPAL",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                color = primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ext.accentTint)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
        if (isEditing && phone.isNotBlank()) {
            IconButton(onClick = onCall, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Call, contentDescription = null, tint = ext.text2, modifier = Modifier.size(18.dp))
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(34.dp)
                .alpha(if (isOnlyOne) 0.35f else 1f),
            enabled = !isOnlyOne,
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = ext.text2, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
internal fun ClienteFieldLabel(text: String, required: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (required) {
            Spacer(Modifier.width(4.dp))
            Text(text = "*", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
internal fun clienteFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.extendedColors.border2,
    unfocusedBorderColor = MaterialTheme.extendedColors.border,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientePhotoPickerSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Text(stringResource(R.string.photo_sheet_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        ListItem(
            headlineContent = { Text(stringResource(R.string.photo_sheet_take_photo)) },
            leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
            modifier = Modifier.clickable { onCamera() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.photo_sheet_choose_gallery)) },
            leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
            modifier = Modifier.clickable { onGallery() },
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}
