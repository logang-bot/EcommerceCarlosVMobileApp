package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun BiometricCard(
    available: Boolean,
    enrolled: Boolean,
    enrolledDate: String?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val cardBg = if (enrolled) ext.accentSoft else ext.surface2
    val cardBorder = if (enrolled) ext.accentTint else ext.border
    val iconBg = if (enrolled) ext.accentTint else ext.surface3
    val iconTint = if (enrolled) MaterialTheme.colorScheme.primary else ext.text2
    val subColor = when {
        !available -> ext.text4
        enrolled -> MaterialTheme.colorScheme.primary
        else -> ext.text3
    }
    val subtitleText = when {
        !available -> stringResource(R.string.perfil_biometrica_not_available)
        enrolled -> stringResource(R.string.perfil_biometrica_active)
        else -> stringResource(R.string.perfil_biometrica_hint)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (available) 1f else 0.55f)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg),
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.perfil_biometrica_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            BiometricToggle(
                on = enrolled,
                onToggle = onToggle,
                enabled = available,
            )
        }
        if (enrolled && enrolledDate != null) {
            HorizontalDivider(color = ext.border)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.perfil_biometrica_registered, enrolledDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text2,
                )
            }
        }
    }
}

@Composable
private fun BiometricToggle(on: Boolean, onToggle: () -> Unit, enabled: Boolean = true) {
    val accent = MaterialTheme.colorScheme.primary
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(if (on) accent else ext.surface3)
            .border(if (on) 0.dp else 1.dp, ext.border2, CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
            .padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BiometricCardEnrolledDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        BiometricCard(
            available = true,
            enrolled = true,
            enrolledDate = "17 de marzo",
            onToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun BiometricCardUnenrolledLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        BiometricCard(
            available = true,
            enrolled = false,
            enrolledDate = null,
            onToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
