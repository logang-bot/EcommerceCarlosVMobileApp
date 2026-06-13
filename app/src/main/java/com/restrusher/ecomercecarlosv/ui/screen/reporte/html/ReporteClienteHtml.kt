package com.restrusher.ecomercecarlosv.ui.screen.reporte.html

import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClientePreset
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClienteUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatPeriodLabel(state: ReporteClienteUiState): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    return when (state.preset) {
        ReporteClientePreset.HOY -> "Hoy · ${df.format(Date(state.fromMs))}"
        ReporteClientePreset.SEMANA, ReporteClientePreset.MES ->
            "${df.format(Date(state.fromMs))} – ${df.format(Date(state.toMs))}"
        ReporteClientePreset.PERSONALIZADO -> {
            val from = if (state.fromMs > 0L) df.format(Date(state.fromMs)) else "—"
            val to = if (state.toMs > 0L) df.format(Date(state.toMs)) else "—"
            "$from – $to"
        }
    }
}

fun buildReporteClienteHtml(
    state: ReporteClienteUiState,
    periodLabel: String,
    generatedDate: String,
): String {
    val df = SimpleDateFormat("dd MMM yyyy", Locale("es"))

    val facturado = state.pedidosInRange.sumOf { it.total }
    val pagado = state.pedidosInRange.sumOf { it.paid }
    val saldo = state.pedidosInRange.sumOf { it.pending }

    val rows = state.pedidosInRange.joinToString("") { pedido ->
        val dateStr = df.format(Date(pedido.createdAt))
        val detalle = when {
            pedido.isSaldoExtra -> "Saldo extra" + if (!pedido.notes.isNullOrBlank()) " · ${pedido.notes}" else ""
            pedido.lines.isNotEmpty() -> pedido.lines.joinToString(", ") { "×${it.quantity} ${it.productName}" }
            else -> "—"
        }
        val (statusLabel, statusClass) = when {
            pedido.isSaldoExtra -> "Saldo extra" to "saldo"
            pedido.status == PedidoStatus.PENDING -> "Pendiente" to "pending"
            pedido.status == PedidoStatus.PARTIAL -> "Parcial" to "partial"
            else -> "Pagado" to "paid"
        }
        val saldoCell = if (pedido.status == PedidoStatus.PAID) {
            "<td class='amt gray'>—</td>"
        } else {
            "<td class='amt amber'>Bs. ${"%.2f".format(pedido.pending)}</td>"
        }
        "<tr>" +
            "<td><b>$dateStr</b></td>" +
            "<td class='det'>$detalle</td>" +
            "<td class='amt'>Bs. ${"%.2f".format(pedido.total)}</td>" +
            "<td class='amt green'>Bs. ${"%.2f".format(pedido.paid)}</td>" +
            "$saldoCell" +
            "<td><span class='chip $statusClass'>$statusLabel</span></td>" +
            "</tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:28px;color:#1a1a1a;font-size:12.5px;line-height:1.45}
.hdr{display:flex;align-items:center;gap:16px;border-bottom:2px solid #1E7D38;padding-bottom:16px;margin-bottom:20px}
.logo{width:46px;height:46px;background:#1E7D38;border-radius:10px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;font-weight:700;flex-shrink:0}
.hdr-info h1{font-size:16px;font-weight:700;color:#1a1a1a}
.hdr-info p{font-size:11.5px;color:#888;margin-top:2px}
.hdr-right{margin-left:auto;text-align:right;font-size:11px;color:#aaa}
h2{font-size:9.5px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#bbb;margin:18px 0 8px}
.info-grid{display:grid;grid-template-columns:1fr 1fr;gap:6px 20px;background:#f8f8f8;border-radius:9px;padding:12px 14px}
.gi .lbl{font-size:9.5px;text-transform:uppercase;letter-spacing:.4px;color:#ccc;margin-bottom:1px}
.gi .val{font-size:12.5px;font-weight:600}
.period{background:#f8f8f8;border-radius:9px;padding:10px 14px;display:flex;justify-content:space-between;align-items:center}
.period .lbl{font-size:9.5px;text-transform:uppercase;letter-spacing:.4px;color:#ccc;margin-bottom:2px}
.period .val{font-size:13px;font-weight:600}
.stats{display:flex;gap:10px;margin-top:8px}
.stat{flex:1;border-radius:8px;padding:10px 12px;border:1px solid #eee}
.stat.f{background:#F2FBF6;border-color:#B3E9C7}.stat.f .sv{color:#16a34a}
.stat.p{background:#EEF4FF;border-color:#C3D3F7}.stat.p .sv{color:#4C8DF5}
.stat.s{background:#FFF8E6;border-color:#FAE09A}.stat.s .sv{color:#D97706}
.sl{font-size:9.5px;text-transform:uppercase;letter-spacing:.4px;color:#aaa;margin-bottom:3px}
.sv{font-size:15px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:6px;font-size:12px}
th{font-size:9px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:5px 8px;border-bottom:1.5px solid #eee;text-align:left}
td{padding:8px 8px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.det{max-width:200px;color:#555;font-size:11.5px}
.amt{font-family:monospace;font-weight:600;text-align:right;white-space:nowrap}
.green{color:#16a34a}.amber{color:#D97706}.gray{color:#ccc}
.tot td{font-weight:700;font-size:13px;background:#fafafa;border-top:1.5px solid #ddd}
.chip{display:inline-block;padding:2px 7px;border-radius:20px;font-size:10px;font-weight:600;white-space:nowrap}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.chip.partial{background:#FFF8E6;color:#D97706}
.chip.paid{background:#F2FBF6;color:#16a34a}
.chip.saldo{background:#FFF8E6;color:#D97706}
.footer{margin-top:28px;padding-top:10px;border-top:1px solid #f0f0f0;font-size:10.5px;color:#ccc;text-align:center}
</style></head><body>

<div class="hdr">
  <div class="logo">CV</div>
  <div class="hdr-info">
    <h1>Comercializadora Carlos V</h1>
    <p>Reporte de pedidos · ${state.clienteName}</p>
  </div>
  <div class="hdr-right"><div>$generatedDate</div></div>
</div>

<h2>Cliente</h2>
<div class="info-grid">
  <div class="gi"><div class="lbl">Nombre</div><div class="val">${state.clienteName}</div></div>
  <div class="gi"><div class="lbl">Mercado</div><div class="val">${state.mercadoName.ifBlank { "—" }}</div></div>
  ${if (state.clienteDesc.isNotBlank()) "<div class=\"gi\"><div class=\"lbl\">Descripción</div><div class=\"val\">${state.clienteDesc}</div></div>" else ""}
  ${if (state.clientePhone.isNotBlank()) "<div class=\"gi\"><div class=\"lbl\">Teléfono</div><div class=\"val\">${state.clientePhone}</div></div>" else ""}
</div>

<h2>Período</h2>
<div class="period">
  <div><div class="lbl">Rango</div><div class="val">$periodLabel</div></div>
  <div style="text-align:right"><div class="lbl">Total pedidos</div><div class="val">${state.pedidosCount}</div></div>
</div>

<h2>Resumen</h2>
<div class="stats">
  <div class="stat f"><div class="sl">Total facturado</div><div class="sv">Bs.&nbsp;${"%.2f".format(facturado)}</div></div>
  <div class="stat p"><div class="sl">Pagado</div><div class="sv">Bs.&nbsp;${"%.2f".format(pagado)}</div></div>
  <div class="stat s"><div class="sl">Saldo pendiente</div><div class="sv">Bs.&nbsp;${"%.2f".format(saldo)}</div></div>
</div>

<h2>Pedidos</h2>
<table>
  <thead>
    <tr>
      <th>Fecha</th><th>Detalle</th>
      <th style="text-align:right">Total</th>
      <th style="text-align:right">Pagado</th>
      <th style="text-align:right">Saldo</th>
      <th>Estado</th>
    </tr>
  </thead>
  <tbody>$rows</tbody>
  <tfoot>
    <tr class="tot">
      <td colspan="2">Total</td>
      <td class="amt">Bs. ${"%.2f".format(facturado)}</td>
      <td class="amt green">Bs. ${"%.2f".format(pagado)}</td>
      <td class="amt amber">Bs. ${"%.2f".format(saldo)}</td>
      <td></td>
    </tr>
  </tfoot>
</table>

<div class="footer">Comercializadora Carlos V &middot; Generado el $generatedDate</div>
</body></html>"""
}
