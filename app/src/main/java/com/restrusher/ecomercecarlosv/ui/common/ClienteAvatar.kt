package com.restrusher.ecomercecarlosv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun ClienteAvatar(
    modifier: Modifier = Modifier,
    name: String,
    size: Dp = 46.dp,
    status: ClientStatus? = null,
) {
    val ext = MaterialTheme.extendedColors
    val hue = name.fold(0) { acc, c -> (acc * 31 + c.code) % 360 }.toFloat()
    val bgColor = Color.hsl(hue = hue, saturation = 0.32f, lightness = 0.26f)
    val textColor = Color.hsl(hue = hue, saturation = 0.55f, lightness = 0.78f)

    val ringColor: Color? = when (status) {
        ClientStatus.CRITICO -> ext.redText
        ClientStatus.ADVERTENCIA -> ext.amberText
        ClientStatus.AL_DIA -> ext.greenText
        null -> null
    }
    val bgSurface = MaterialTheme.colorScheme.background

    val initials = name.split(' ')
        .filter(String::isNotBlank)
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawBehind {
                if (ringColor != null) {
                    drawCircle(color = bgSurface, radius = this.size.minDimension / 2 + 4.dp.toPx())
                    drawCircle(color = ringColor, radius = this.size.minDimension / 2 + 2.dp.toPx())
                }
            }
            .clip(CircleShape)
            .background(bgColor),
    ) {
        Text(
            text = initials,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.3.sp,
        )
    }
}
