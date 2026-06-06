package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun TotalModeCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ext.accentTint else ext.surface2)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else ext.border,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.6.dp, ext.border3, CircleShape),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (selected) ext.text2 else ext.text3,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        trailingContent?.invoke()
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun TotalModeCardSelectedPreview() {
    EcomerceCarlosVTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TotalModeCard(
                selected = true,
                onClick = {},
                title = "Calcular automáticamente",
                subtitle = "Suma todos los pedidos pendientes",
                trailingContent = {
                    Text(
                        text = "Bs. 219.50",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            TotalModeCard(
                selected = false,
                onClick = {},
                title = "Ingresar manualmente",
                subtitle = "Escribe un monto distinto",
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TotalModeCardSelectedDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TotalModeCard(
                selected = true,
                onClick = {},
                title = "Calcular automáticamente",
                subtitle = "Suma todos los pedidos pendientes",
                trailingContent = {
                    Text(
                        text = "Bs. 219.50",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            TotalModeCard(
                selected = false,
                onClick = {},
                title = "Ingresar manualmente",
                subtitle = "Escribe un monto distinto",
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun TotalModeCardManualPreview() {
    EcomerceCarlosVTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TotalModeCard(
                selected = false,
                onClick = {},
                title = "Calcular automáticamente",
                subtitle = "Suma todos los pedidos pendientes",
            )
            TotalModeCard(
                selected = true,
                onClick = {},
                title = "Ingresar manualmente",
                subtitle = "Escribe un monto distinto",
            )
        }
    }
}
