package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ClienteStatusBadge(
    modifier: Modifier = Modifier,
    status: ClientStatus,
    size: BadgeSize = BadgeSize.SM,
) {
    val ext = MaterialTheme.extendedColors
    val (bgColor, textColor, dotColor, label) = when (status) {
        ClientStatus.CRITICO -> BadgeStyle(ext.redTint, ext.redText, MaterialTheme.colorScheme.error, stringResource(R.string.status_critico))
        ClientStatus.ADVERTENCIA -> BadgeStyle(ext.amberTint, ext.amberText, ext.amber, stringResource(R.string.status_advertencia))
        ClientStatus.AL_DIA -> BadgeStyle(ext.greenTint, ext.greenText, ext.green, stringResource(R.string.status_al_dia))
    }
    val dotSize = if (size == BadgeSize.SM) 5.dp else 6.dp
    val fontSize = if (size == BadgeSize.SM) 11.5.sp else 13.sp
    val hPad = if (size == BadgeSize.SM) 8.dp else 11.dp
    val vPad = if (size == BadgeSize.SM) 3.dp else 5.dp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = hPad, vertical = vPad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(if (size == BadgeSize.SM) 5.dp else 6.dp))
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            lineHeight = 1.sp,
        )
    }
}

enum class BadgeSize { SM, MD }

private data class BadgeStyle(
    val bg: androidx.compose.ui.graphics.Color,
    val text: androidx.compose.ui.graphics.Color,
    val dot: androidx.compose.ui.graphics.Color,
    val label: String,
)
