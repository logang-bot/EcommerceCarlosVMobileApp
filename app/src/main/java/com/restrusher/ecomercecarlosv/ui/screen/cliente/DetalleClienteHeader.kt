package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun BlacklistBanner(blacklistedAt: Long?) {
    val ext = MaterialTheme.extendedColors
    val dateText = blacklistedAt?.let {
        SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(it))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(ext.redTint)
            .border(1.dp, Color(0x47F05A50), RoundedCornerShape(13.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(
            Icons.Default.Block,
            contentDescription = null,
            tint = ext.redText,
            modifier = Modifier.size(20.dp).padding(top = 1.dp),
        )
        Column {
            Text(
                text = stringResource(R.string.detalle_cliente_lista_negra_title),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = ext.redText,
            )
            Text(
                text = if (dateText != null) {
                    stringResource(R.string.detalle_cliente_lista_negra_subtitle, dateText)
                } else {
                    stringResource(R.string.detalle_cliente_lista_negra_subtitle, "—")
                },
                fontSize = 12.5.sp,
                color = ext.text2,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
internal fun ClienteHeader(cliente: Cliente) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            ClienteAvatar(name = cliente.name, size = 76.dp)
            if (cliente.isBlacklisted) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF05A50))
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            cliente.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp,
        )
        if (cliente.description.isNotBlank()) {
            Text(
                text = cliente.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.extendedColors.text2,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.padding(top = 14.dp),
        ) {
            cliente.phones.getOrElse(cliente.primaryPhoneIndex) { cliente.phones.firstOrNull() ?: "" }.ifBlank { null }?.let { phone ->
                ContactChip(
                    icon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    label = phone,
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                    },
                )
            }
            if (!cliente.mapsUrl.isNullOrBlank()) {
                ContactChip(
                    icon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    label = stringResource(R.string.detalle_cliente_ubicacion),
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cliente.mapsUrl))) }
                    },
                )
            }
        }
    }
}

@Composable
private fun ContactChip(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border2, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        icon()
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, fontSize = 13.5.sp)
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewCliente = Cliente(
    id = "c1", mercadoId = "m1", name = "Ana Rodríguez",
    description = "Puesto 14 · verduras", phones = listOf("0414-2230198"),
    mapsUrl = "https://maps.google.com/?q=Mercado+Central", createdAt = 0L,
)

@Preview(name = "ClienteHeader — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ClienteHeaderPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            ClienteHeader(cliente = previewCliente)
        }
    }
}

@Preview(name = "ClienteHeader blacklisted — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ClienteHeaderBlacklistedPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            ClienteHeader(cliente = previewCliente.copy(isBlacklisted = true))
        }
    }
}

@Preview(name = "BlacklistBanner — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BlacklistBannerPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            BlacklistBanner(blacklistedAt = 1747526400000L)
        }
    }
}
