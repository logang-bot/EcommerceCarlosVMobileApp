package com.restrusher.ecomercecarlosv.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun PedidosTopBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    large: Boolean = false,
    actions: @Composable () -> Unit = {},
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .padding(horizontal = if (onBack != null) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
                if (!large) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                start = if (onBack != null) 2.dp else 12.dp,
                                top = if (subtitle != null) 9.dp else 0.dp,
                                bottom = if (subtitle != null) 9.dp else 0.dp,
                            ),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.extendedColors.text2,
                                modifier = Modifier.padding(top = 1.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                actions()
            }
            if (large) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineLarge)
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.extendedColors.text2,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.extendedColors.border)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PedidosTopBarPreview() {
    EcomerceCarlosVTheme {
        PedidosTopBar(title = "Pedido", subtitle = "28 may 2026", onBack = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PedidosTopBarDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        PedidosTopBar(title = "Pedido", subtitle = "28 may 2026", onBack = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PedidosTopBarNoSubtitlePreview() {
    EcomerceCarlosVTheme {
        PedidosTopBar(title = "Mercados", onBack = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun PedidosTopBarLargePreview() {
    EcomerceCarlosVTheme {
        Column {
            PedidosTopBar(title = "Clientes", subtitle = "Mercado de Coche", large = true)
            Spacer(Modifier.height(8.dp))
        }
    }
}
