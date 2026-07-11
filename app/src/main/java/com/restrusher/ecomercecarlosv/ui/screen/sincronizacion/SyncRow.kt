package com.restrusher.ecomercecarlosv.ui.screen.sincronizacion

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.data.local.entity.EntityType
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun relativeTime(epochMs: Long): String {
    val diff = System.currentTimeMillis() - epochMs
    return when {
        diff < 60_000 -> stringResource(R.string.sinc_time_just_now)
        diff < 3_600_000 -> stringResource(R.string.sinc_time_minutes, diff / 60_000)
        diff < 86_400_000 -> stringResource(R.string.sinc_time_hours, diff / 3_600_000)
        else -> stringResource(R.string.sinc_time_days, diff / 86_400_000)
    }
}

@Composable
fun syncOperationLabel(entityType: String, operation: String): String = when {
    entityType == EntityType.PEDIDO && operation == SyncOp.UPSERT -> stringResource(R.string.sinc_op_pedido)
    entityType == EntityType.PEDIDO && operation == SyncOp.DELETE -> stringResource(R.string.sinc_op_pedido_delete)
    entityType == EntityType.CLIENTE && operation == SyncOp.UPSERT -> stringResource(R.string.sinc_op_cliente)
    entityType == EntityType.CLIENTE && operation == SyncOp.DELETE -> stringResource(R.string.sinc_op_cliente_delete)
    entityType == EntityType.MERCADO && operation == SyncOp.UPSERT -> stringResource(R.string.sinc_op_mercado)
    entityType == EntityType.MERCADO && operation == SyncOp.DELETE -> stringResource(R.string.sinc_op_mercado_delete)
    entityType == EntityType.PRODUCTO && operation == SyncOp.UPSERT -> stringResource(R.string.sinc_op_producto)
    entityType == EntityType.PRODUCTO && operation == SyncOp.DELETE -> stringResource(R.string.sinc_op_producto_delete)
    entityType == EntityType.UMBRALES && operation == SyncOp.UPSERT -> stringResource(R.string.sinc_op_umbrales)
    else -> entityType
}

@Composable
fun SyncRow(item: SyncQueueItem, isError: Boolean) {
    val ext = MaterialTheme.extendedColors
    val rowIcon: ImageVector = when (item.entityType) {
        EntityType.PEDIDO -> Icons.Default.ShoppingCart
        EntityType.CLIENTE -> Icons.Default.Person
        EntityType.MERCADO -> Icons.Default.Store
        EntityType.PRODUCTO -> Icons.Default.Sell
        EntityType.UMBRALES -> Icons.Default.Info
        else -> Icons.Default.Sync
    }
    val opLabel = syncOperationLabel(item.entityType, item.operation)
    val subText = "${item.entityLabel.ifBlank { opLabel }} · ${relativeTime(item.createdAt)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, ext.border, RoundedCornerShape(11.dp)),
        ) {
            Icon(
                imageVector = rowIcon,
                contentDescription = null,
                tint = ext.text2,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = opLabel,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subText,
                fontSize = 12.5.sp,
                color = ext.text3,
            )
        }
        if (isError) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ext.amberTint)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(ext.amber, CircleShape),
                )
                Text(
                    text = stringResource(R.string.sinc_chip_not_sent),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ext.amberText,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sinc_chip_queued),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ext.text3,
                )
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

@Composable
fun FooterNote() {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = ext.text4,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = stringResource(R.string.sinc_footer_note),
            fontSize = 12.sp,
            color = ext.text3,
            lineHeight = (12 * 1.45).sp,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────

private val sampleItemPending = SyncQueueItem(
    id = 1,
    entityType = EntityType.PEDIDO,
    operation = SyncOp.UPSERT,
    entityLabel = "Bs. 240,00",
    createdAt = System.currentTimeMillis() - 3 * 60_000,
    retryCount = 0,
)

private val sampleItemError = sampleItemPending.copy(
    entityType = EntityType.CLIENTE,
    operation = SyncOp.UPSERT,
    entityLabel = "Ana Rodríguez",
    retryCount = 2,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "SyncRow En cola Light")
@Composable
private fun SyncRowQueuedPreview() {
    EcomerceCarlosVTheme {
        Surface { SyncRow(item = sampleItemPending, isError = false) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "SyncRow En cola Dark")
@Composable
private fun SyncRowQueuedDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SyncRow(item = sampleItemPending, isError = false) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "SyncRow Sin enviar Light")
@Composable
private fun SyncRowErrorPreview() {
    EcomerceCarlosVTheme {
        Surface { SyncRow(item = sampleItemError, isError = true) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "SyncRow Sin enviar Dark")
@Composable
private fun SyncRowErrorDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface { SyncRow(item = sampleItemError, isError = true) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "FooterNote")
@Composable
private fun FooterNotePreview() {
    EcomerceCarlosVTheme {
        Surface { FooterNote() }
    }
}
