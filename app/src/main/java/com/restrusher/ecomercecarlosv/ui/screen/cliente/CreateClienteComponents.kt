package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.io.File

@Composable
internal fun CirclePhotoPicker(modifier: Modifier = Modifier, photoUri: Uri?, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
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
            if (photoUri != null) {
                ClienteAvatar(name = "", size = 96.dp)
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp, end = 4.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
internal fun PhoneListField(
    modifier: Modifier = Modifier,
    phones: List<String>,
    onPhoneChange: (Int, String) -> Unit,
    onAddPhone: () -> Unit,
    onRemovePhone: (Int) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ClienteFieldLabel(text = stringResource(R.string.create_cliente_phones_label))
        phones.forEachIndexed { index, phone ->
            OutlinedTextField(
                value = phone,
                onValueChange = { onPhoneChange(index, it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ext.text2, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (phones.size > 1) {
                    { IconButton(onClick = { onRemovePhone(index) }) { Icon(Icons.Default.Close, contentDescription = null, tint = ext.text2) } }
                } else null,
                placeholder = { Text(stringResource(R.string.create_cliente_phone_placeholder), color = ext.text3) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                colors = clienteFieldColors(),
            )
        }
        TextButton(onClick = onAddPhone, modifier = Modifier.padding(start = 2.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.create_cliente_add_phone), fontWeight = FontWeight.Medium)
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

internal fun createClienteCameraUri(context: android.content.Context): Uri {
    val imagesDir = File(context.cacheDir, "images").also { it.mkdirs() }
    val imageFile = File(imagesDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}
