package com.restrusher.ecomercecarlosv.ui.screen.reporte

data class PendingExport(
    val html: String,
    val fileName: String,
    val itemCount: Int,
    val isMovimientosVariant: Boolean,
)

object ReporteExportHolder {
    var pending: PendingExport? = null
}
