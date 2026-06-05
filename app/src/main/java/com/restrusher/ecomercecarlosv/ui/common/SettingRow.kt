package com.restrusher.ecomercecarlosv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    iconColor: Color? = null,
    iconBg: Color? = null,
    subtitle: String? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) = SettingRowImpl(
    iconContent = { tint ->
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    },
    title = title,
    modifier = modifier,
    iconColor = iconColor,
    iconBg = iconBg,
    subtitle = subtitle,
    danger = danger,
    onClick = onClick,
    trailing = trailing,
)

@Composable
fun SettingRow(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    iconColor: Color? = null,
    iconBg: Color? = null,
    subtitle: String? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) = SettingRowImpl(
    iconContent = { tint ->
        Icon(painter = icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    },
    title = title,
    modifier = modifier,
    iconColor = iconColor,
    iconBg = iconBg,
    subtitle = subtitle,
    danger = danger,
    onClick = onClick,
    trailing = trailing,
)

@Composable
private fun SettingRowImpl(
    iconContent: @Composable (tint: Color) -> Unit,
    title: String,
    modifier: Modifier,
    iconColor: Color?,
    iconBg: Color?,
    subtitle: String?,
    danger: Boolean,
    onClick: (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
) {
    val ext = MaterialTheme.extendedColors
    val resolvedIconColor = iconColor ?: ext.text2
    val resolvedIconBg = iconBg ?: ext.surface3

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(resolvedIconBg),
        ) {
            iconContent(resolvedIconColor)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (danger) ext.redText else MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text3,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        trailing?.invoke()
    }
}
