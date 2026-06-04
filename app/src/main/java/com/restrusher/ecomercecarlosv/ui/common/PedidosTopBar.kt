package com.restrusher.ecomercecarlosv.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                    .height(52.dp)
                    .padding(horizontal = if (onBack != null) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
                if (!large) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (onBack != null) 2.dp else 12.dp),
                    )
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
                            style = MaterialTheme.typography.bodySmall,
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
