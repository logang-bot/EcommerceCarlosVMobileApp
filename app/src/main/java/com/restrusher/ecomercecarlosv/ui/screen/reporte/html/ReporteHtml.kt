package com.restrusher.ecomercecarlosv.ui.screen.reporte.html

import com.restrusher.ecomercecarlosv.ui.screen.reporte.MovimientoType
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteMode
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun buildReporteHtml(state: ReporteUiState, generatedDate: String): String =
    if (state.mode == ReporteMode.DIARIO) buildDiarioHtml(state, generatedDate)
    else buildPorClienteHtml(state, generatedDate)

fun buildDiarioHtml(state: ReporteUiState, generatedDate: String): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val fromStr = df.format(Date(state.diarioFromMs))
    val toStr = df.format(Date(state.diarioToMs))
    val rangeStr = if (fromStr == toStr) fromStr else "$fromStr – $toStr"

    val movRows = state.movimientos.joinToString("") { mov ->
        val typeLabel = if (mov.type == MovimientoType.COBRO) "Cobro" else "Pedido"
        val typeClass = if (mov.type == MovimientoType.COBRO) "paid" else "pending"
        val timeStr = SimpleDateFormat("HH:mm", Locale("es")).format(Date(mov.timestamp))
        "<tr><td><b>${mov.clienteName.ifBlank { "—" }}</b>" +
            "<div class='sub'>$typeLabel · ${mov.mercadoName} · $timeStr</div></td>" +
            "<td><span class='chip $typeClass'>$typeLabel</span></td>" +
            "<td class='amt'>Bs. ${"%.2f".format(mov.amount)}</td></tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
h2{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.7px;color:#aaa;margin:20px 0 8px}
.sum{display:flex;gap:10px;margin:8px 0}
.sc{flex:1;border-radius:9px;padding:11px 13px}
.sc.g{background:#F2FBF6;border:1px solid #B3E9C7}.sc.g .sv{color:#16a34a}
.sc.a{background:#FFF8E6;border:1px solid #FAE09A}.sc.a .sv{color:#D97706}
.sc.b{background:#EEF4FF;border:1px solid #C3D3F7}.sc.b .sv{color:#4C8DF5}
.sl{font-size:10px;text-transform:uppercase;letter-spacing:.4px;color:#888;margin-bottom:4px}
.sv{font-size:20px;font-weight:700;font-family:monospace}
table{width:100%;border-collapse:collapse;margin-top:4px}
th{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:#bbb;padding:6px 10px;border-bottom:1px solid #eee;text-align:left}
td{padding:9px 10px;border-bottom:1px solid #f5f5f5;vertical-align:top}
tr:last-child td{border-bottom:none}
.sub{font-size:10.5px;color:#999;margin-top:3px}
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.chip{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10.5px;font-weight:600;white-space:nowrap}
.chip.paid{background:#F2FBF6;color:#16a34a}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <div><h1>Reporte diario</h1><div class="meta">Comercializadora Carlos V &middot; $rangeStr</div></div>
  <div class="meta" style="text-align:right">$generatedDate</div>
</div>
<h2>Resumen</h2>
<div class="sum">
  <div class="sc g"><div class="sl">Cobrado</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.cobradoTotal)}</div></div>
  <div class="sc b"><div class="sl">Pedidos creados</div><div class="sv">${state.pedidosCreadosCount}</div></div>
  <div class="sc a"><div class="sl">Pendiente del día</div><div class="sv">Bs.&nbsp;${"%.2f".format(state.pendienteDelDia)}</div></div>
</div>
<h2>Movimientos</h2>
${
        if (movRows.isEmpty()) "<p style='color:#aaa;font-size:12px'>Sin movimientos en este período.</p>"
        else "<table><thead><tr><th>Cliente</th><th>Tipo</th><th style='text-align:right'>Monto</th></tr></thead><tbody>$movRows</tbody></table>"
    }
<div class="footer">Comercializadora Carlos V &middot; Generado el $generatedDate</div>
</body></html>"""
}

fun buildPorClienteHtml(state: ReporteUiState, generatedDate: String): String {
    val df = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val fromStr = df.format(Date(state.clienteFromMs))
    val toStr = df.format(Date(state.clienteToMs))

    val historialRows = state.historial.joinToString("") { item ->
        val statusLabel = when {
            item.isSaldoExtra -> "Extra"
            item.pending == 0.0 -> "Pagado"
            item.paid > 0 -> "Parcial"
            else -> "Pendiente"
        }
        val statusClass = when {
            item.isSaldoExtra || (item.paid > 0 && item.pending > 0) -> "partial"
            item.pending == 0.0 -> "paid"
            else -> "pending"
        }
        val pendingCell = if (item.pending > 0)
            "<td class='amt amber'>Bs. ${"%.2f".format(item.pending)}</td>"
        else "<td class='amt gray'>—</td>"
        "<tr><td><b>${item.title}</b></td><td><span class='chip $statusClass'>$statusLabel</span></td>" +
            "<td class='amt'>Bs. ${"%.2f".format(item.total)}</td>$pendingCell</tr>"
    }

    return """<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:Arial,sans-serif;padding:32px;color:#1a1a1a;font-size:13px;line-height:1.45}
.hdr{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:2.5px solid #2FA24E;padding-bottom:16px;margin-bottom:22px}
.hdr h1{font-size:20px;font-weight:700;letter-spacing:-0.3px}
.meta{font-size:11px;color:#888;margin-top:3px}
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
.amt{font-family:monospace;font-weight:700;text-align:right;white-space:nowrap}
.amber{color:#D97706}.gray{color:#ccc}
.chip{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10.5px;font-weight:600;white-space:nowrap}
.chip.pending{background:#EEF4FF;color:#4C8DF5}
.chip.partial{background:#FFF8E6;color:#D97706}
.chip.paid{background:#F2FBF6;color:#16a34a}
.footer{margin-top:32px;padding-top:12px;border-top:1px solid #f0f0f0;font-size:11px;color:#ccc;text-align:center}
</style></head><body>
<div class="hdr">
  <div><h1>Reporte por cliente</h1><div class="meta">Comercializadora Carlos V &middot; ${state.selectedClienteName} &middot; ${state.selectedMercadoName}</div></div>
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
        else "<table><thead><tr><th>Pedido</th><th>Estado</th><th style='text-align:right'>Total</th><th style='text-align:right'>Pendiente</th></tr></thead><tbody>$historialRows</tbody></table>"
    }
<div class="footer">Comercializadora Carlos V &middot; ${state.selectedClienteName} &middot; Generado el $generatedDate</div>
</body></html>"""
}
