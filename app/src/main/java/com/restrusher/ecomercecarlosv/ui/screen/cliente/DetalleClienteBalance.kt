package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun BalanceBlock(status: ClientStatus, balance: Double, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val (gradStart, gradEnd, borderColor) = when (status) {
        ClientStatus.CRITICO    -> Triple(Color(0x26F05A50), Color(0x0AF05A50), Color(0x38F05A50))
        ClientStatus.ADVERTENCIA -> Triple(Color(0x26E7B23E), Color(0x0AE7B23E), Color(0x38E7B23E))
        ClientStatus.AL_DIA     -> Triple(Color(0x2436C880), Color(0x0836C880), Color(0x3336C880))
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors = listOf(gradStart, gradEnd)))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                stringResource(R.string.detalle_cliente_saldo_total),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = formatBalance(balance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        ClienteStatusBadge(status = status, size = BadgeSize.MD)
    }
}

@Composable
internal fun BlacklistBalanceBlock(balance: Double) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors = listOf(Color(0x26F05A50), Color(0x0AF05A50))))
            .border(1.dp, Color(0x38F05A50), RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(13.dp))
                Text(
                    stringResource(R.string.detalle_cliente_monto_vetado_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text2,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = formatBalance(balance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp,
                color = ext.redText,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                stringResource(R.string.detalle_cliente_monto_vetado_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = ext.text3,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "BalanceBlock — all statuses dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BalanceBlockPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                BalanceBlock(status = ClientStatus.CRITICO, balance = 340.0)
                Spacer(Modifier.height(12.dp))
                BalanceBlock(status = ClientStatus.ADVERTENCIA, balance = 85.5)
                Spacer(Modifier.height(12.dp))
                BalanceBlock(status = ClientStatus.AL_DIA, balance = 0.0)
            }
        }
    }
}

@Preview(name = "BalanceBlock — all statuses light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun BalanceBlockLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                BalanceBlock(status = ClientStatus.CRITICO, balance = 340.0)
                Spacer(Modifier.height(12.dp))
                BalanceBlock(status = ClientStatus.ADVERTENCIA, balance = 85.5)
                Spacer(Modifier.height(12.dp))
                BalanceBlock(status = ClientStatus.AL_DIA, balance = 0.0)
            }
        }
    }
}
