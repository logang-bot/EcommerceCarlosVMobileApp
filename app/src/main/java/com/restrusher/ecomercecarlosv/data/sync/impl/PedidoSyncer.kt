package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PagoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.mapper.DetallePedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PagoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PedidoMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.DetallePedidoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.PagoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.PedidoDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncParentResolver
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class PedidoSyncer @Inject constructor(
    private val pedidoDao: PedidoDao,
    private val detalleDao: DetallePedidoDao,
    private val pagoDao: PagoDao,
    private val parentResolver: SyncParentResolver,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val pedidoDtos = fetchPedidos(since)
            val upsertedIds = upsertPedidos(pedidoDtos)
            val detalleCount = syncDetalles(since, upsertedIds)
            val pagoCount = syncPagos(since, upsertedIds)
            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${pedidoDtos.size} pedidos, $detalleCount detalles, $pagoCount pagos")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    private suspend fun fetchPedidos(since: Long): List<PedidoDto> =
        if (since == 0L) {
            fetchAllPedidoPages()
        } else {
            supabase.from("pedidos").select { filter { gt("updated_at", since) } }.decodeList()
        }

    /** Returns the ids now present in Room. A pedido whose cliente could not be recovered is left
     *  out rather than inserted, which would fail the foreign key and abort the whole pull. */
    private suspend fun upsertPedidos(dtos: List<PedidoDto>): List<String> {
        val (tombstones, live) = dtos.partition { it.isDeleted }
        tombstones.forEach { pedidoDao.deleteById(it.id) }
        val clientes = parentResolver.ensureClientesExist(live.mapTo(mutableSetOf()) { it.clienteId })
        val (writable, orphans) = live.partition { it.clienteId in clientes }
        if (orphans.isNotEmpty()) Log.w(TAG, "skipped ${orphans.size} pedido(s) with no local cliente")
        return writable.mapNotNull { upsert(it) }
    }

    /** Returns the pedido id when it is now in Room, or null when it was skipped — its detalles and
     *  pagos must be skipped too, since both foreign-key onto `pedidos`. */
    private suspend fun upsert(dto: PedidoDto): String? {
        return runCatching {
            val entity = PedidoMapper.fromDto(dto)
            if (pedidoDao.insert(entity) == -1L) pedidoDao.update(entity)
            dto.id
        }.getOrElse {
            Log.e(TAG, "skipping pedido ${dto.id}: write failed", it)
            null
        }
    }

    private suspend fun syncDetalles(since: Long, upsertedIds: List<String>): Int {
        val dtos: List<DetallePedidoDto> = if (since == 0L) {
            fetchAllDetallPages()
        } else {
            if (upsertedIds.isEmpty()) return 0
            upsertedIds.forEach { detalleDao.deleteByPedido(it) }
            supabase.from("detalle_pedido").select {
                filter { isIn("pedido_id", upsertedIds) }
            }.decodeList()
        }
        val writable = withKnownPedido(dtos) { it.pedidoId }
        if (writable.isNotEmpty()) detalleDao.insertAll(writable.map(DetallePedidoMapper::fromDto))
        return writable.size
    }

    private suspend fun syncPagos(since: Long, upsertedIds: List<String>): Int {
        val dtos: List<PagoDto> = if (since == 0L) {
            fetchAllPagoPages()
        } else {
            if (upsertedIds.isEmpty()) return 0
            upsertedIds.forEach { pagoDao.deleteByPedido(it) }
            supabase.from("pagos").select {
                filter { isIn("pedido_id", upsertedIds) }
            }.decodeList()
        }
        val writable = withKnownPedido(dtos) { it.pedidoId }
        if (writable.isNotEmpty()) pagoDao.insertAll(writable.map(PagoMapper::fromDto))
        return writable.size
    }

    /** The full-fetch path pulls every line regardless of whether its pedido made it into Room, so
     *  the parent has to be confirmed locally before inserting. */
    private suspend fun <T> withKnownPedido(dtos: List<T>, pedidoId: (T) -> String): List<T> {
        if (dtos.isEmpty()) return dtos
        val known = pedidoDao.existingIds(dtos.map(pedidoId).distinct()).toSet()
        val writable = dtos.filter { pedidoId(it) in known }
        if (writable.size < dtos.size) {
            Log.w(TAG, "skipped ${dtos.size - writable.size} line(s) with no local pedido")
        }
        return writable
    }

    private suspend fun fetchAllPedidoPages(): List<PedidoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("pedidos").select {
                filter { eq("is_deleted", false) }
                // Offset paging without a stable sort can skip rows between pages.
                order("id", Order.ASCENDING)
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<PedidoDto>()
            addAll(page)
            if (page.size < BATCH_SIZE) break
            offset += BATCH_SIZE
        }
    }

    private suspend fun fetchAllDetallPages(): List<DetallePedidoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("detalle_pedido").select {
                order("id", Order.ASCENDING)
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<DetallePedidoDto>()
            addAll(page)
            if (page.size < BATCH_SIZE) break
            offset += BATCH_SIZE
        }
    }

    private suspend fun fetchAllPagoPages(): List<PagoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("pagos").select {
                order("id", Order.ASCENDING)
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<PagoDto>()
            addAll(page)
            if (page.size < BATCH_SIZE) break
            offset += BATCH_SIZE
        }
    }

    companion object {
        private const val TAG = "PedidoSyncer"
        private const val BATCH_SIZE = 1000
    }
}
