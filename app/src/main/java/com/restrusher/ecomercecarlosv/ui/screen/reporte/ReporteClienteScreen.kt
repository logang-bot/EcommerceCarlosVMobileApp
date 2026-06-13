package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PayChip
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReporteClienteScreen(navController: NavController) {
    val viewModel = hiltViewModel<ReporteClienteViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val onExportarPdf = remember(state.clienteName) {
        {
            val dateStr = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())
            val html = buildReporteHtml(state, dateStr)
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = false
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "Reporte_${state.clienteName.replace(" ", "_")}"
                    pm.print(jobName, view.createPrintDocumentAdapter(jobName), PrintAttributes.Builder().build())
                }
            }
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    }

    ReporteClienteContent(
        state = state,
        onBack = { navController.popBackStack() },
        onExportarPdf = onExportarPdf,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteClienteContent(
    state: ReporteClienteUiState,
    onBack: () -> Unit,
    onExportarPdf: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.reporte_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                        )
                        if (state.clienteName.isNotBlank()) {
                            Text(
                                text = state.clienteName,
                                fontSize = 12.sp,
                                color = MaterialTheme.extendedColors.text3,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ReporteEncabezado()
                ReporteClienteInfoSection(state)
                ReporteResumenSection(state)
                ReportePedidosSection(state.pedidos)
            }

            ReporteExportBar(onExportarPdf = onExportarPdf)
        }
    }
}

// ── Sections ────────────────────────────────────────────────────────────────

@Composable
private fun ReporteEncabezado() {
    val ext = MaterialTheme.extendedColors
    val today = remember {
        SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ext.accentSoft)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(ext.accentTint),
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column {
            Text(
                text = stringResource(R.string.reporte_encabezado_empresa),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Text(
                text = stringResource(R.string.reporte_encabezado_fecha, today),
                fontSize = 12.sp,
                color = ext.text3,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ReporteClienteInfoSection(state: ReporteClienteUiState) {
    val ext = MaterialTheme.extendedColors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReporteSectionLabel(stringResource(R.string.reporte_seccion_cliente))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReporteInfoItem(
                    label = stringResource(R.string.reporte_cliente_nombre),
                    value = state.clienteName,
                    modifier = Modifier.weight(1f),
                )
                ReporteInfoItem(
                    label = stringResource(R.string.reporte_cliente_mercado),
                    value = state.mercadoName.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReporteInfoItem(
                    label = stringResource(R.string.reporte_cliente_descripcion),
                    value = state.clienteDesc.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                )
                ReporteInfoItem(
                    label = stringResource(R.string.reporte_cliente_telefono),
                    value = state.clientePhone.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReporteResumenSection(state: ReporteClienteUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReporteSectionLabel(stringResource(R.string.reporte_seccion_resumen))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReporteResumenCard(
                label = stringResource(R.string.reporte_resumen_sin_pagar),
                value = state.unpaidCount.toString(),
                valueColor = MaterialTheme.extendedColors.blueText,
                bgColor = MaterialTheme.extendedColors.blueTint,
                modifier = Modifier.weight(1f),
            )
            ReporteResumenCard(
                label = stringResource(R.string.reporte_resumen_saldo),
                value = "Bs. ${"%.2f".format(state.balance)}",
                valueColor = MaterialTheme.extendedColors.amberText,
                bgColor = MaterialTheme.extendedColors.amberTint,
                modifier = Modifier.weight(1f),
            )
            ReporteResumenCard(
                label = stringResource(R.string.reporte_resumen_total),
                value = state.totalCount.toString(),
                valueColor = MaterialTheme.extendedColors.greenText,
                bgColor = MaterialTheme.extendedColors.greenTint,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReportePedidosSection(pedidos: List<Pedido>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReporteSectionLabel(stringResource(R.string.reporte_seccion_pedidos))
        if (pedidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.extendedColors.surface2)
                    .border(1.dp, MaterialTheme.extendedColors.border, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.reporte_pedidos_vacio),
                    fontSize = 13.sp,
                    color = MaterialTheme.extendedColors.text3,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.extendedColors.surface2)
                    .border(1.dp, MaterialTheme.extendedColors.border, RoundedCornerShape(16.dp)),
            ) {
                pedidos.forEachIndexed { index, pedido ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.extendedColors.border)
                    ReportePedidoRow(pedido = pedido)
                }
            }
        }
    }
}

// ── Small composables ────────────────────────────────────────────────────────

@Composable
private fun ReporteSectionLabel(text: String) {
    val ext = MaterialTheme.extendedColors
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = ext.text3,
        letterSpacing = 0.5.sp,
    )
}

@Composable
private fun ReporteInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    Column(modifier = modifier) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = ext.text4, letterSpacing = 0.3.sp)
        Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun ReporteResumenCard(
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.extendedColors.text3)
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.5).sp,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReportePedidoRow(pedido: Pedido) {
    val ext = MaterialTheme.extendedColors
    val dateStr = remember(pedido.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date(pedido.createdAt))
    }
    val isPartial = pedido.status == PedidoStatus.PARTIAL
    val bgColor = if (pedido.isSaldoExtra) Color(0x0BE7B23E) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 11.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = if (pedido.isSaldoExtra) stringResource(R.string.pedidos_row_saldo_extra) else dateStr,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!pedido.isSaldoExtra) {
                    Text(
                        text = stringResource(R.string.pedidos_row_n_productos, pedido.itemCount),
                        fontSize = 12.sp,
                        color = ext.text3,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                } else if (!pedido.notes.isNullOrBlank()) {
                    Text(
                        text = dateStr + " · " + pedido.notes,
                        fontSize = 12.sp,
                        color = ext.text3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (isPartial) {
                    Text(
                        text = "Bs. ${"%.2f".format(pedido.pending)}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = ext.amberText,
                    )
                } else {
                    Text(
                        text = "Bs. ${"%.2f".format(pedido.total)}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                PayChip(status = pedido.status)
            }
        }

        if (!pedido.isSaldoExtra && pedido.lines.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = pedido.lines.joinToString(" · ") { "×${it.quantity} ${it.productName}" },
                fontSize = 11.5.sp,
                color = ext.text4,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Export bar ───────────────────────────────────────────────────────────────

@Composable
private fun ReporteExportBar(onExportarPdf: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = ext.border, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .navigationBarsPadding(),
    ) {
        Button(
            onClick = onExportarPdf,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.reporte_exportar_pdf),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── HTML builder ─────────────────────────────────────────────────────────────

private fun buildReporteHtml(state: ReporteClienteUiState, generatedDate: String): String {
    val df = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val pedidoRows = state.pedidos.joinToString("") { pedido ->
        val dateStr = df.format(Date(pedido.createdAt))
        val isPartial = pedido.status == PedidoStatus.PARTIAL
        val (statusLabel, statusClass) = when {
            pedido.isSaldoExtra -> "Saldo extra" to "saldo"
            pedido.status == PedidoStatus.PENDING -> "Pendiente" to "pending"
            pedido.status == PedidoStatus.PARTIAL -> "Parcial" to "partial"
            else -> "Pagado" to "paid"
        }
        val subLine = when {
            !pedido.isSaldoExtra && pedido.lines.isNotEmpty() ->
                "<div class='sub'>${pedido.lines.joinToString(" &middot; ") { "&times;${it.quantity} ${it.productName}" }}</div>"
            pedido.isSaldoExtra && !pedido.notes.isNullOrBlank() ->
                "<div class='sub'>${pedido.notes}</div>"
            else -> ""
        }
        val pendingCell = when {
            pedido.status == PedidoStatus.PAID -> "<td class='amt gray'>—</td>"
            isPartial -> "<td class='amt amber'>Bs. ${"%.2f".format(pedido.pending)}</td>"
            else -> "<td class='amt'>Bs. ${"%.2f".format(pedido.pending)}</td>"
        }
        "<tr><td><b>$dateStr</b>$subLine</td><td><span class='chip $statusClass'>$statusLabel</span></td><td class='amt'>Bs. ${"%.2f".format(pedido.total)}</td>$pendingCell</tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
h2{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#aaa;margin:20px 0 8px}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:8px 20px;background:#f8f8f8;border-radius:10px;padding:14px 16px}
.gi .lbl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#bbb;margin-bottom:2px}
.gi .val{font-size:13.5px;font-weight:600}
.sum{display:flex;gap:10px;margin:8px 0}
.sc{flex:1;border-radius:9px;padding:11px 13px}
.sc.u{background:#EEF4FF;border:1px solid #C3D3F7}.sc.u .sv{color:#4C8DF5}
.sc.b{background:#FFF8E6;border:1px solid #FAE09A}.sc.b .sv{color:#D97706}
.sc.t{background:#F2FBF6;border:1px solid #B3E9C7}.sc.t .sv{color:#16a34a}
.sl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#888;margin-bottom:4px}
.sv{font-size:20px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:4px}
th{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:6px 10px;border-bottom:1px solid #eee;text-align:left}
td{padding:9px 10px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.sub{font-size:10.5px;color:#999;margin-top:3px}
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.amber{color:#D97706}.gray{color:#ccc}
.chip{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10.5px;font-weight:600;white-space:nowrap}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.chip.partial{background:#FFF8E6;color:#D97706}
.chip.paid{background:#F2FBF6;color:#16a34a}
.chip.saldo{background:#FFF8E6;color:#D97706}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <div><h1>Reporte de pedidos</h1><div class="meta">Comercializadora Carlos V &middot; ${state.clienteName}</div></div>
  <div class="meta" style="text-align:right">$generatedDate</div>
</div>
<h2>Cliente</h2>
<div class="grid">
  <div class="gi"><div class="lbl">Nombre</div><div class="val">${state.clienteName}</div></div>
  <div class="gi"><div class="lbl">Mercado</div><div class="val">${state.mercadoName.ifBlank { "—" }}</div></div>
  <div class="gi"><div class="lbl">Descripción</div><div class="val">${state.clienteDesc.ifBlank { "—" }}</div></div>
  ${if (state.clientePhone.isNotBlank()) "<div class=\"gi\"><div class=\"lbl\">Teléfono</div><div class=\"val\">${state.clientePhone}</div></div>" else ""}
</div>
<h2>Resumen</h2>
<div class="sum">
  <div class="sc u"><div class="sl">Sin pagar</div><div class="sv">${state.unpaidCount}</div></div>
  <div class="sc b"><div class="sl">Saldo pendiente</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.balance)}</div></div>
  <div class="sc t"><div class="sl">Total pedidos</div><div class="sv">${state.totalCount}</div></div>
</div>
<h2>Pedidos</h2>
<table><thead><tr><th>Fecha</th><th>Estado</th><th style="text-align:right">Total</th><th style="text-align:right">Pendiente</th></tr></thead>
<tbody>$pedidoRows</tbody></table>
<div class="footer">Comercializadora Carlos V &middot; Generado el $generatedDate</div>
</body></html>"""
}
