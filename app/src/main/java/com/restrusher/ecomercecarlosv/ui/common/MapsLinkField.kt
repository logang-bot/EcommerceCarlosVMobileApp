package com.restrusher.ecomercecarlosv.ui.common

import android.content.Intent
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

/**
 * Maps URL field used in both edit forms (editable=true) and detail screens (editable=false).
 *
 * - Editable: inline text input with a "Pegar" chip that reads the clipboard.
 * - Read-only: displays the URL with an "Abrir" chip that opens it in the Maps app.
 */
@Composable
fun MapsLinkField(
    value: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    hint: String? = null,
    editable: Boolean = true,
    onValueChange: (String) -> Unit = {},
) {
    val ext = MaterialTheme.extendedColors
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val filled = value.isNotBlank()
    val borderColor = if (filled) ext.border2 else ext.border
    val pinBg = if (filled) ext.accentTint else ext.surface3
    val pinTint = if (filled) MaterialTheme.colorScheme.primary else ext.text3
    val shape = RoundedCornerShape(14.dp)

    Column(modifier = modifier) {
        if (label != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (required) {
                    Spacer(Modifier.width(4.dp))
                    Text(text = "*", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(shape)
                .background(ext.surface2)
                .border(1.dp, borderColor, shape)
                .padding(start = 14.dp, end = 10.dp, top = 0.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(pinBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = pinTint, modifier = Modifier.size(18.dp))
            }

            if (editable) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                    textStyle = TextStyle(
                        fontSize = 14.5.sp,
                        fontWeight = if (filled) FontWeight.Medium else FontWeight.Normal,
                        color = if (filled) MaterialTheme.colorScheme.onSurface else ext.text3,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.maps_field_placeholder),
                                fontSize = 14.5.sp,
                                color = ext.text3,
                            )
                        }
                        inner()
                    },
                )
            } else {
                Text(
                    text = if (filled) value else stringResource(R.string.maps_field_placeholder),
                    fontSize = 14.5.sp,
                    fontWeight = if (filled) FontWeight.Medium else FontWeight.Normal,
                    color = if (filled) MaterialTheme.colorScheme.onSurface else ext.text3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            val chipShape = RoundedCornerShape(9.dp)
            if (filled) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(chipShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
                            }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.maps_field_open),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            } else if (editable) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(chipShape)
                        .background(ext.surface3)
                        .border(1.dp, ext.border2, chipShape)
                        .clickable {
                            clipboard.getText()?.text?.let { onValueChange(it) }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.maps_field_paste),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ext.text2,
                    )
                }
            }
        }

        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                modifier = Modifier.padding(top = 7.dp),
            )
        }
    }
}
