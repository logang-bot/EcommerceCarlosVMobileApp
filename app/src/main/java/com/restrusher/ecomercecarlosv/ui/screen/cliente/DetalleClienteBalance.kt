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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tag
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

// ── Main block ────────────────────────────────────────────────────────────────

@Composable
internal fun BalanceBlock(
    modifier: Modifier = Modifier,
    status: ClientStatus,
    balance: Double,
    isManualBlacklisted: Boolean = false,
) {
    val ext = MaterialTheme.extendedColors
    val (gradStart, gradEnd, borderColor) = if (isManualBlacklisted) {
        Triple(Color(0x29F05A50), Color(0x0AF05A50), Color(0x47F05A50))
    } else {
        when (status) {
            ClientStatus.CRITICO     -> Triple(Color(0x26F05A50), Color(0x0AF05A50), Color(0x38F05A50))
            ClientStatus.ADVERTENCIA -> Triple(Color(0x26E7B23E), Color(0x0AE7B23E), Color(0x38E7B23E))
            ClientStatus.AL_DIA      -> Triple(Color(0x2436C880), Color(0x0836C880), Color(0x3336C880))
        }
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
                text = if (isManualBlacklisted) {
                    stringResource(R.string.detalle_cliente_saldo_ln)
                } else {
                    stringResource(R.string.detalle_cliente_saldo_total)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = formatBalance(balance),
                fontSize = if (isManualBlacklisted) 29.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp,
                color = if (isManualBlacklisted) ext.redText else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (isManualBlacklisted) {
            ManualBadge(ext.redText)
        } else {
            ClienteStatusBadge(status = status, size = BadgeSize.MD)
        }
    }
}

@Composable
private fun ManualBadge(redText: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0x1FF05A50))
            .border(1.5.dp, Color(0x40F05A50), CircleShape)
            .padding(start = 7.dp, end = 9.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.Block, contentDescription = null, tint = redText, modifier = Modifier.size(13.dp))
        Text(
            text = stringResource(R.string.detalle_cliente_balance_manual_badge),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = redText,
        )
    }
}

// ── Manual caption ────────────────────────────────────────────────────────────

@Composable
internal fun BalanceCaption(modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = ext.text3, modifier = Modifier.size(14.dp))
        Text(
            text = stringResource(R.string.detalle_cliente_balance_manual_caption),
            fontSize = 12.sp,
            color = ext.text3,
            lineHeight = 17.sp,
        )
    }
}

// ── Breakdown cards ───────────────────────────────────────────────────────────

internal enum class BalanceCardKind {
    PEDIDOS, EXTRA, LN;

    val icon: ImageVector get() = when (this) {
        PEDIDOS -> Icons.Default.Receipt
        EXTRA   -> Icons.Default.Tag
        LN      -> Icons.Default.Block
    }
}

@Composable
internal fun BalanceCard(
    modifier: Modifier = Modifier,
    kind: BalanceCardKind,
    amount: Double,
    sub: String,
    wide: Boolean = false,
    inactive: Boolean = false,
    badge: String? = null,
) {
    val ext = MaterialTheme.extendedColors
    val (activeColor, activeTint, activeRing) = when (kind) {
        BalanceCardKind.PEDIDOS -> Triple(ext.blueText, ext.blueTint, Color(0x384C8DF5))
        BalanceCardKind.EXTRA   -> Triple(ext.amberText, ext.amberTint, Color(0x38E7B23E))
        BalanceCardKind.LN      -> Triple(ext.redText, ext.redTint, Color(0x38F05A50))
    }
    val bgColor = if (inactive) MaterialTheme.colorScheme.surface else activeTint
    val borderColor = if (inactive) ext.border else activeRing
    val iconTint = if (inactive) ext.text3 else activeColor
    val labelColor = if (inactive) ext.text3 else activeColor
    val amountColor = if (inactive) ext.text3 else activeColor

    Column(
        modifier = modifier
            .then(if (wide) Modifier.fillMaxWidth() else Modifier)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(15.dp))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(kind.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
            Text(
                text = when (kind) {
                    BalanceCardKind.PEDIDOS -> stringResource(R.string.detalle_cliente_balance_pedidos_label)
                    BalanceCardKind.EXTRA   -> stringResource(R.string.detalle_cliente_balance_extra_label)
                    BalanceCardKind.LN      -> stringResource(R.string.detalle_cliente_balance_ln_label)
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
            )
            if (badge != null) {
                Text(
                    text = badge.uppercase(),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (inactive) ext.surface2 else activeTint)
                        .border(1.dp, if (inactive) ext.border2 else activeRing, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (inactive) ext.text3 else activeColor,
                    letterSpacing = 0.3.sp,
                )
            }
        }
        Text(
            text = formatBalance(amount),
            fontSize = if (wide) 23.sp else 19.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.6).sp,
            color = amountColor,
            textDecoration = if (inactive) TextDecoration.LineThrough else TextDecoration.None,
        )
        Text(
            text = sub,
            fontSize = 11.sp,
            color = if (inactive) ext.text4 else ext.text3,
            lineHeight = 14.sp,
        )
    }
}

@Composable
internal fun BalanceBreakdown(
    modifier: Modifier = Modifier,
    pedidosBalance: Double,
    unpaidPedidosCount: Int,
    extraBalance: Double,
    unpaidExtraCount: Int,
    isBlacklisted: Boolean,
    isManualAmount: Boolean,
) {
    val pedidosSub = if (isManualAmount) {
        stringResource(R.string.detalle_cliente_balance_frozen)
    } else {
        stringResource(R.string.detalle_cliente_balance_pedidos_sub, unpaidPedidosCount)
    }
    val extraSub = if (isManualAmount) {
        stringResource(R.string.detalle_cliente_balance_frozen)
    } else {
        if (unpaidExtraCount == 1) {
            stringResource(R.string.detalle_cliente_balance_extra_sub_one)
        } else {
            stringResource(R.string.detalle_cliente_balance_extra_sub_other, unpaidExtraCount)
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BalanceCard(
                modifier = Modifier.weight(1f),
                kind = BalanceCardKind.PEDIDOS,
                amount = pedidosBalance,
                sub = pedidosSub,
                inactive = isManualAmount,
            )
            BalanceCard(
                modifier = Modifier.weight(1f),
                kind = BalanceCardKind.EXTRA,
                amount = extraBalance,
                sub = extraSub,
                inactive = isManualAmount,
            )
        }
        if (isBlacklisted && !isManualAmount) {
            BalanceCard(
                kind = BalanceCardKind.LN,
                amount = pedidosBalance + extraBalance,
                sub = stringResource(R.string.detalle_cliente_balance_ln_sub),
                wide = true,
                badge = stringResource(R.string.detalle_cliente_balance_auto_badge),
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
                Spacer(Modifier.height(12.dp))
                BalanceBlock(status = ClientStatus.CRITICO, balance = 340.0, isManualBlacklisted = true)
            }
        }
    }
}

@Preview(name = "BalanceBreakdown — normal dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BalanceBreakdownPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                BalanceBreakdown(
                    pedidosBalance = 280.0, unpaidPedidosCount = 3,
                    extraBalance = 60.0, unpaidExtraCount = 1,
                    isBlacklisted = false, isManualAmount = false,
                )
                Spacer(Modifier.height(16.dp))
                BalanceBreakdown(
                    pedidosBalance = 280.0, unpaidPedidosCount = 3,
                    extraBalance = 60.0, unpaidExtraCount = 1,
                    isBlacklisted = true, isManualAmount = false,
                )
                Spacer(Modifier.height(16.dp))
                BalanceBreakdown(
                    pedidosBalance = 280.0, unpaidPedidosCount = 3,
                    extraBalance = 60.0, unpaidExtraCount = 1,
                    isBlacklisted = true, isManualAmount = true,
                )
            }
        }
    }
}
