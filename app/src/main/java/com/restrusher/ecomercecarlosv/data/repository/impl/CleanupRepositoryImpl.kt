package com.restrusher.ecomercecarlosv.data.repository.impl

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.remote.dto.PedidoDto
import com.restrusher.ecomercecarlosv.domain.repository.CleanupRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class CleanupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pedidoDao: PedidoDao,
    private val pedidoRepository: PedidoRepository,
    private val supabase: SupabaseClient,
) : CleanupRepository {

    override suspend fun countPedidosOlderThan(cutoffMs: Long): Int {
        // The local table may be empty/stale if the user opens this screen before any other
        // pedido-reading screen triggered a sync, so refresh before counting.
        pedidoRepository.refresh()
        return pedidoDao.countByCreatedAtBefore(cutoffMs)
    }

    override suspend fun exportPedidosToFile(
        cutoffMs: Long,
        useXlsx: Boolean,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Triple<String, Long, Uri> {
        val allPedidos = fetchPedidosFromSupabase(cutoffMs, onProgress)
        val ext = if (useXlsx) "xlsx" else "csv"
        val dateLabel = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(cutoffMs))
        val fileName = "pedidos_hasta_$dateLabel.$ext"
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "depuracion")
        dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { fos ->
            if (useXlsx) writeXlsx(fos, allPedidos) else writeCsv(fos, allPedidos)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Triple(fileName, file.length(), uri)
    }

    override suspend fun deletePedidosFromCloud(
        cutoffMs: Long,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Int {
        onProgress(0, 1)
        val count = pedidoDao.countByCreatedAtBefore(cutoffMs)
        supabase.from("pedidos").delete {
            filter { lt("created_at", cutoffMs) }
        }
        pedidoDao.deleteByCreatedAtBefore(cutoffMs)
        onProgress(1, 1)
        return count
    }

    private suspend fun fetchPedidosFromSupabase(
        cutoffMs: Long,
        onProgress: (current: Int, total: Int) -> Unit,
    ): List<PedidoDto> {
        val result = mutableListOf<PedidoDto>()
        var offset = 0L
        var total = pedidoDao.countByCreatedAtBefore(cutoffMs).coerceAtLeast(1)
        while (true) {
            val page = supabase.from("pedidos").select {
                filter { lt("created_at", cutoffMs) }
                range(offset, offset + 999)
            }.decodeList<PedidoDto>()
            result.addAll(page)
            total = total.coerceAtLeast(result.size)
            onProgress(result.size, total)
            if (page.size < 1000) break
            offset += 1000
        }
        return result
    }

    private fun writeCsv(out: OutputStream, pedidos: List<PedidoDto>) {
        val writer = out.bufferedWriter(Charsets.UTF_8)
        writer.write("id,cliente_id,status,total,paid,notes,is_saldo_extra,created_at,paid_at\n")
        pedidos.forEach { p ->
            writer.write("${p.id},${p.clienteId},${p.status},${p.total},${p.paid}," +
                "${p.notes?.escapeCsv() ?: ""},${p.isSaldoExtra},${p.createdAt},${p.paidAt ?: ""}\n")
        }
        writer.flush()
    }

    private fun String.escapeCsv(): String =
        if (contains(",") || contains("\"") || contains("\n")) "\"${replace("\"", "\"\"")}\"" else this

    private fun writeXlsx(out: OutputStream, pedidos: List<PedidoDto>) {
        val headers = listOf("ID", "Cliente ID", "Estado", "Total", "Pagado", "Notas", "Saldo Extra", "Creado", "Pagado en")
        val rows = pedidos.map { p ->
            listOf(
                p.id, p.clienteId, p.status,
                p.total.toString(), p.paid.toString(),
                p.notes ?: "", p.isSaldoExtra.toString(),
                p.createdAt.toString(), p.paidAt?.toString() ?: "",
            )
        }
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(CONTENT_TYPES_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(RELS_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(WORKBOOK_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(WORKBOOK_RELS_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(buildSheetXml(headers, rows).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }

    private fun buildSheetXml(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        sb.append("<sheetData>")
        val allRows = listOf(headers) + rows
        allRows.forEachIndexed { rowIdx, row ->
            sb.append("<row r=\"${rowIdx + 1}\">")
            row.forEachIndexed { colIdx, cell ->
                val col = colLetter(colIdx)
                val ref = "$col${rowIdx + 1}"
                val safe = cell.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                sb.append("<c r=\"$ref\" t=\"inlineStr\"><is><t>$safe</t></is></c>")
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun colLetter(index: Int): String {
        var i = index
        val sb = StringBuilder()
        do {
            sb.insert(0, ('A' + i % 26))
            i = i / 26 - 1
        } while (i >= 0)
        return sb.toString()
    }

    companion object {
        private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

        private const val RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        private const val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Pedidos" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

        private const val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
    }
}
