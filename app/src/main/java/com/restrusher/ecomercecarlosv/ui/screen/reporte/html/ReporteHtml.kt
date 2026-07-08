package com.restrusher.ecomercecarlosv.ui.screen.reporte.html

import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteMode
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun buildReporteHtml(state: ReporteUiState, generatedDate: String, logoDataUri: String): String =
    if (state.mode == ReporteMode.DIARIO) buildDiarioHtml(state, generatedDate, logoDataUri)
    else buildPorClienteHtml(state, generatedDate, logoDataUri)

fun buildDiarioHtml(state: ReporteUiState, generatedDate: String, logoDataUri: String): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val fromStr = df.format(Date(state.diarioFromMs))
    val toStr = df.format(Date(state.diarioToMs))
    val rangeStr = if (fromStr == toStr) fromStr else "$fromStr – $toStr"

    val rows = state.diarioPedidos.joinToString("") { item ->
        val pendingCell = if (item.pending > 0)
            "<td class='amt amber'>Bs. ${"%.2f".format(item.pending)}</td>"
        else "<td class='amt gray'>—</td>"
        "<tr><td><b>${item.title.ifBlank { "—" }}</b>" +
            "<div class='sub'>${item.subtitle}</div></td>" +
            "<td class='amt'>Bs. ${"%.2f".format(item.total)}</td>" +
            "<td class='amt green'>Bs. ${"%.2f".format(item.paid)}</td>" +
            "$pendingCell</tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;align-items:center;gap:14px;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr .logo{width:48px;height:48px;object-fit:contain;flex-shrink:0}
.hdr-info{flex:1}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
h2{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#aaa;margin:20px 0 8px}
.sum{display:flex;gap:10px;margin:8px 0}
.sc{flex:1;border-radius:9px;padding:11px 13px}
.sc.p{background:#EEF4FF;border:1px solid #C3D3F7}.sc.p .sv{color:#4C8DF5}
.sc.g{background:#F2FBF6;border:1px solid #B3E9C7}.sc.g .sv{color:#16a34a}
.sc.a{background:#FFF8E6;border:1px solid #FAE09A}.sc.a .sv{color:#D97706}
.sl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#888;margin-bottom:4px}
.sv{font-size:20px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:4px}
th{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:6px 10px;border-bottom:1px solid #eee;text-align:left}
td{padding:9px 10px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.sub{font-size:10.5px;color:#999;margin-top:3px}
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.green{color:#16a34a}.amber{color:#D97706}.gray{color:#ccc}
.tot td{font-weight:700;font-size:13px;background:#fafafa;border-top:1.5px solid #ddd}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <img class="logo" src="$logoDataUri" alt="Logo" />
  <div class="hdr-info"><h1>Reporte diario</h1><div class="meta">Comercializadora Carlos V &middot; $rangeStr</div></div>
  <div class="meta" style="text-align:right">$generatedDate</div>
</div>
<h2>Resumen</h2>
<div class="sum">
  <div class="sc p"><div class="sl">Total pedidos</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.diarioFacturado)}</div></div>
  <div class="sc g"><div class="sl">Pagado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.diarioPagado)}</div></div>
  <div class="sc a"><div class="sl">Pendiente</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.diarioPendiente)}</div></div>
</div>
<h2>Pedidos</h2>
${
        if (rows.isEmpty()) "<p style='color:#aaa;font-size:12px'>Sin pedidos en este período.</p>"
        else """<table><thead><tr><th>Cliente</th><th style='text-align:right'>Total</th><th style='text-align:right'>Pagado</th><th style='text-align:right'>Pendiente</th></tr></thead><tbody>$rows</tbody>
<tfoot><tr class="tot"><td>Total</td><td class="amt">Bs. ${"%.2f".format(state.diarioFacturado)}</td><td class="amt green">Bs. ${"%.2f".format(state.diarioPagado)}</td><td class="amt amber">Bs. ${"%.2f".format(state.diarioPendiente)}</td></tr></tfoot></table>"""
    }
<div class="footer">Comercializadora Carlos V &middot; Generado el $generatedDate</div>
</body></html>"""
}

fun buildPorClienteHtml(state: ReporteUiState, generatedDate: String, logoDataUri: String): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val fromStr = df.format(Date(state.clienteFromMs))
    val toStr = df.format(Date(state.clienteToMs))

    val historialRows = state.historial.joinToString("") { item ->
        val statusLabel = when {
            item.pending == 0.0 -> "Pagado"
            item.paid > 0 -> "Parcial"
            else -> "Pendiente"
        }
        val statusClass = when {
            item.paid > 0 && item.pending > 0 -> "partial"
            item.pending == 0.0 -> "paid"
            else -> "pending"
        }
        val detalle = if (item.lines.isNotEmpty()) {
            "<ul>" + item.lines.joinToString("") { "<li>×${it.quantity} ${it.productName}</li>" } + "</ul>"
        } else "—"
        val pendingCell = if (item.pending > 0)
            "<td class='amt amber'>Bs. ${"%.2f".format(item.pending)}</td>"
        else "<td class='amt gray'>—</td>"
        "<tr><td><b>${item.title}</b></td><td class='det'>$detalle</td>" +
            "<td class='amt'>Bs. ${"%.2f".format(item.total)}</td>" +
            "<td class='amt green'>Bs. ${"%.2f".format(item.paid)}</td>" +
            "$pendingCell" +
            "<td><span class='chip $statusClass'>$statusLabel</span></td></tr>"
    }

    val extraRows = state.saldoExtras.joinToString("") { item ->
        val pendingCell = if (item.pending > 0)
            "<td class='amt amber'>Bs. ${"%.2f".format(item.pending)}</td>"
        else "<td class='amt gray'>—</td>"
        "<tr><td><b>${item.title}</b><div class='sub'>${item.subtitle}</div></td>" +
            "<td class='amt'>Bs. ${"%.2f".format(item.total)}</td>" +
            "<td class='amt green'>Bs. ${"%.2f".format(item.paid)}</td>" +
            "$pendingCell</tr>"
    }
    val extraFacturado = state.saldoExtras.sumOf { it.total }
    val extraPagado = state.saldoExtras.sumOf { it.paid }
    val extraSaldo = state.saldoExtras.sumOf { it.pending }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;align-items:center;gap:14px;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr .logo{width:48px;height:48px;object-fit:contain;flex-shrink:0}
.hdr-info{flex:1}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
.client-meta{font-size:15px;font-weight:700;color:#1a1a1a;margin-top:4px}
h2{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#aaa;margin:20px 0 8px}
.sum{display:flex;gap:10px;margin:8px 0}
.sc{flex:1;border-radius:9px;padding:11px 13px}
.sc.g{background:#F2FBF6;border:1px solid #B3E9C7}.sc.g .sv{color:#16a34a}
.sc.a{background:#FFF8E6;border:1px solid #FAE09A}.sc.a .sv{color:#D97706}
.sc.p{background:#EEF4FF;border:1px solid #C3D3F7}.sc.p .sv{color:#4C8DF5}
.sl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#888;margin-bottom:4px}
.sv{font-size:20px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:4px}
th{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:6px 10px;border-bottom:1px solid #eee;text-align:left}
td{padding:9px 10px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.det{max-width:220px;color:#555;font-size:11.5px}
.det ul{list-style:disc;margin:0 0 0 14px;padding:0}
.det ul li{margin-top:2px}
.det ul li:first-child{margin-top:0}
.sub{font-size:10.5px;color:#999;margin-top:3px}
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.green{color:#16a34a}.amber{color:#D97706}.gray{color:#ccc}
.tot td{font-weight:700;font-size:13px;background:#fafafa;border-top:1.5px solid #ddd}
.chip{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10.5px;font-weight:600;white-space:nowrap}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.chip.partial{background:#FFF8E6;color:#D97706}
.chip.paid{background:#F2FBF6;color:#16a34a}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <img class="logo" src="$logoDataUri" alt="Logo" />
  <div class="hdr-info">
    <h1>Reporte por cliente</h1>
    <div class="meta">Comercializadora Carlos V</div>
    <div class="client-meta">${state.selectedClienteName} &middot; ${state.selectedMercadoName}</div>
  </div>
  <div class="meta" style="text-align:right">$fromStr – $toStr<br>$generatedDate</div>
</div>
<h2>Resumen</h2>
<div class="sum">
  <div class="sc p"><div class="sl">Facturado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.facturado)}</div></div>
  <div class="sc g"><div class="sl">Pagado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.pagado)}</div></div>
  <div class="sc a"><div class="sl">Saldo</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.saldo)}</div></div>
</div>
<h2>Historial</h2>
${
        if (historialRows.isEmpty()) "<p style='color:#aaa;font-size:12px'>Sin transacciones en este período.</p>"
        else """<table><thead><tr><th>Pedido</th><th>Detalle</th><th style='text-align:right'>Total</th><th style='text-align:right'>Pagado</th><th style='text-align:right'>Saldo</th><th>Estado</th></tr></thead><tbody>$historialRows</tbody>
<tfoot><tr class="tot"><td colspan="2">Total</td><td class="amt">Bs. ${"%.2f".format(state.facturado)}</td><td class="amt green">Bs. ${"%.2f".format(state.pagado)}</td><td class="amt amber">Bs. ${"%.2f".format(state.saldo)}</td><td></td></tr></tfoot></table>"""
    }
${
        if (state.saldoExtras.isEmpty()) "" else """
<h2>Saldo extra</h2>
<table><thead><tr><th>Detalle</th><th style='text-align:right'>Total</th><th style='text-align:right'>Pagado</th><th style='text-align:right'>Saldo</th></tr></thead><tbody>$extraRows</tbody>
<tfoot><tr class="tot"><td>Total</td><td class="amt">Bs. ${"%.2f".format(extraFacturado)}</td><td class="amt green">Bs. ${"%.2f".format(extraPagado)}</td><td class="amt amber">Bs. ${"%.2f".format(extraSaldo)}</td></tr></tfoot></table>"""
    }

<div class="footer">Comercializadora Carlos V &middot; ${state.selectedClienteName} &middot; Generado el $generatedDate</div>
</body></html>"""
}
