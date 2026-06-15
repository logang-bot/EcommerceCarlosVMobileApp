package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.presentation.screens.ClientesRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import com.restrusher.ecomercecarlosv.ui.common.MapsLinkField
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.PhotoThumbnail
import com.restrusher.ecomercecarlosv.ui.common.SettingRow
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun DetalleMercadoScreen(
    navController: NavController,
    viewModel: DetalleMercadoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleMercadoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onEditClick = { mercadoId -> navController.navigate(CreateMercadoRoute(mercadoId)) },
        onDelete = { viewModel.onDelete { navController.popBackStack() } },
        onClientesClick = { navController.navigate(ClientesRoute(state.mercadoId)) },
    )
}

@Composable
private fun DetalleMercadoContent(
    state: DetalleMercadoUiState,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDelete: () -> Unit,
    onClientesClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.detalle_mercado_title),
                onBack = onBack,
                actions = {
                    if (state.mercado != null && state.canWrite) {
                        IconButton(onClick = { onEditClick(state.mercado.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            state.mercado != null -> MercadoDataContent(
                mercado = state.mercado,
                innerPadding = innerPadding,
                canWrite = state.canWrite,
                onDelete = onDelete,
                onClientesClick = onClientesClick,
            )
        }
    }
}

@Composable
private fun MercadoDataContent(
    mercado: Mercado,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    canWrite: Boolean,
    onDelete: () -> Unit,
    onClientesClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            PhotoThumbnail(
                photoUrl = mercado.photoUrl,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ext.surface3),
                fallback = {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = ext.text2, modifier = Modifier.size(26.dp))
                },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mercado.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (mercado.address.isNotBlank()) {
                    Text(
                        text = mercado.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.text2,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }

        // ── Stats ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(label = stringResource(R.string.detalle_mercado_stat_clientes), value = "0", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            StatCard(label = stringResource(R.string.detalle_mercado_stat_al_dia), value = "0", color = ext.greenText, modifier = Modifier.weight(1f))
            StatCard(label = stringResource(R.string.detalle_mercado_stat_en_riesgo), value = "0", color = ext.redText, modifier = Modifier.weight(1f))
        }

        // ── Ubicación ────────────────────────────────────────────────
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp)) {
            Text(
                text = stringResource(R.string.detalle_mercado_section_ubicacion).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ext.text3,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(10.dp))
            MapsLinkField(
                value = mercado.mapsUrl ?: "",
                hint = stringResource(R.string.detalle_mercado_maps_hint),
                editable = false,
            )
        }

        // ── Meta rows ───────────────────────────────────────────────
        Spacer(Modifier.height(20.dp))
        SettingRow(
            icon = Icons.Default.Person,
            title = stringResource(R.string.detalle_mercado_clientes_activos),
            subtitle = stringResource(R.string.mercados_clientes_active, 0),
            onClick = onClientesClick,
            trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text3, modifier = Modifier.size(17.dp)) },
        )
        HorizontalDivider(color = ext.border, modifier = Modifier.padding(start = 69.dp))
        SettingRow(
            icon = Icons.Default.Receipt,
            title = stringResource(R.string.detalle_mercado_creado),
            subtitle = formatEpochMillis(mercado.createdAt),
        )

        // ── Danger zone ─────────────────────────────────────────────
        if (canWrite) Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)) {
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.extendedColors.redTint,
                    contentColor = MaterialTheme.extendedColors.redText,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.extendedColors.redText.copy(alpha = 0.22f)),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.create_mercado_delete), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = stringResource(R.string.detalle_mercado_delete_confirm),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text3,
                modifier = Modifier.padding(top = 9.dp).fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ext.surface2,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ext.text2,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

private fun formatEpochMillis(millis: Long): String {
    val date = java.util.Date(millis)
    return java.text.SimpleDateFormat("d 'de' MMMM 'de' yyyy", java.util.Locale("es")).format(date)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetalleMercadoScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DetalleMercadoContent(
            DetalleMercadoUiState(
                mercado = Mercado("1", "Mercado de Coche", "Av. Intercomunal, Coche", mapsUrl = "https://maps.google.com/?q=Mercado+de+Coche", createdAt = System.currentTimeMillis()),
                isLoading = false,
            ),
            {}, {}, {}, {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DetalleMercadoScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DetalleMercadoContent(
            DetalleMercadoUiState(
                mercado = Mercado("1", "Mercado de Coche", "Av. Intercomunal, Coche", mapsUrl = "https://maps.google.com/?q=Mercado+de+Coche", createdAt = System.currentTimeMillis()),
                isLoading = false,
            ),
            {}, {}, {}, {}
        )
    }
}
