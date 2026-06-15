package com.restrusher.ecomercecarlosv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val ext = MaterialTheme.extendedColors
    val bgColor = when (role) {
        UserRole.SUPERUSUARIO -> ext.banana
        UserRole.INVITADO -> ext.blueTint
        else -> ext.accentTint
    }
    val textColor = when (role) {
        UserRole.SUPERUSUARIO -> ext.onBanana
        UserRole.INVITADO -> ext.blueText
        else -> MaterialTheme.colorScheme.primary
    }
    val label = when (role) {
        UserRole.SUPERUSUARIO -> stringResource(R.string.role_super)
        UserRole.INVITADO -> stringResource(R.string.role_invitado)
        else -> stringResource(R.string.role_usuario)
    }
    val fontSize = if (small) 11.sp else 12.sp
    val iconSize = if (small) 12.dp else 13.dp
    val padH = if (small) 8.dp else 10.dp
    val padV = if (small) 3.dp else 4.dp

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (role != UserRole.SUPERUSUARIO) Modifier.border(1.dp, ext.border2, CircleShape) else Modifier
            )
            .padding(horizontal = padH, vertical = padV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (small) 5.dp else 6.dp),
    ) {
        when (role) {
            UserRole.SUPERUSUARIO -> Icon(
                painter = painterResource(R.drawable.ic_admin_panel),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize),
            )
            UserRole.INVITADO -> Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize),
            )
            else -> Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize),
            )
        }
        Text(
            text = label,
            fontSize = fontSize,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
        )
    }
}

@Composable
fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    photoUrl: String? = null,
    size: Int = 30,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.extendedColors.border2, CircleShape),
    ) {
        PhotoThumbnail(
            photoUrl = photoUrl,
            modifier = Modifier.fillMaxSize(),
            fallback = {
                Text(
                    text = initials.take(2),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = (size * 0.36).sp,
                )
            },
        )
    }
}
