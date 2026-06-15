package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun ActionButtons(
    isBlacklisted: Boolean,
    canWrite: Boolean,
    onListaNegraClick: () -> Unit,
    onQuitarListaNegraClick: () -> Unit,
    onSaldoExtraClick: () -> Unit,
) {
    if (!canWrite) return
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isBlacklisted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth().height(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                    .clickable(onClick = onQuitarListaNegraClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.clientes_quitar_lista_negra), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth().height(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ext.redTint)
                    .border(1.dp, ext.redText.copy(alpha = 0.25f), RoundedCornerShape(13.dp))
                    .clickable(onClick = onListaNegraClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.clientes_agregar_lista_negra), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = ext.redText)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth().height(46.dp)
                .then(if (isBlacklisted) Modifier.alpha(0.5f) else Modifier)
                .clip(RoundedCornerShape(13.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                .then(if (!isBlacklisted) Modifier.clickable(onClick = onSaldoExtraClick) else Modifier)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Tag, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.clientes_agregar_saldo_extra), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "ActionButtons — normal dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ActionButtonsPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            ActionButtons(
                isBlacklisted = false,
                onListaNegraClick = {}, onQuitarListaNegraClick = {}, onSaldoExtraClick = {},
            )
        }
    }
}

@Preview(name = "ActionButtons — blacklisted dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ActionButtonsBlacklistedPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            ActionButtons(
                isBlacklisted = true,
                onListaNegraClick = {}, onQuitarListaNegraClick = {}, onSaldoExtraClick = {},
            )
        }
    }
}
