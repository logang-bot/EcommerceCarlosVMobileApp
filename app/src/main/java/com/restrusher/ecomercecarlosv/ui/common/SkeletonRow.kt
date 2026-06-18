package com.restrusher.ecomercecarlosv.ui.common

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

/** Animated placeholder row matching the layout of a ClienteRow (avatar + 2 text lines + badge/balance). */
@Composable
fun SkeletonClienteRow(modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(ext.surface3),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ext.surface3),
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(ext.surface3),
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ext.surface3),
            )
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ext.surface3),
            )
        }
    }
    Spacer(Modifier.height(0.dp)) // divider placeholder handled by caller
}

@Preview(name = "Skeleton row – dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Skeleton row – light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun SkeletonClienteRowPreview() {
    EcomerceCarlosVTheme {
        SkeletonClienteRow()
    }
}
