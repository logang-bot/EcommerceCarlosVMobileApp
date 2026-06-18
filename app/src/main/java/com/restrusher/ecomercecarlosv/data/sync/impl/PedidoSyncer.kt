package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.mapper.DetallePedidoMapper
import com.restrusher.ecomercecarlosv.data.mapper.PedidoMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.DetallePedidoDto
import com.restrusher.ecomercecarlosv.data.remote.dto.PedidoDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class PedidoSyncer @Inject constructor(
    private val pedidoDao: PedidoDao,
    private val detalleDao: DetallePedidoDao,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val pedidoDtos = if (since == 0L) {
                fetchAllPedidoPages()
            } else {
                supabase.from("pedidos").select {
                    filter { gt("updated_at", since) }
                }.decodeList<PedidoDto>()
            }
            pedidoDtos.forEach { dto -> pedidoDao.insert(PedidoMapper.fromDto(dto)) }

            val detalleDtos: List<DetallePedidoDto> = if (since == 0L) {
                fetchAllDetallPages()
            } else {
                // Re-fetch lines only for changed pedidos; delete stale lines first.
                val changedIds = pedidoDtos.map { it.id }
                if (changedIds.isEmpty()) return@runCatching SyncResult.Success
                changedIds.forEach { detalleDao.deleteByPedido(it) }
                supabase.from("detalle_pedido").select {
                    filter { isIn("pedido_id", changedIds) }
                }.decodeList()
            }
            if (detalleDtos.isNotEmpty()) {
                detalleDao.insertAll(detalleDtos.map(DetallePedidoMapper::fromDto))
            }

            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${pedidoDtos.size} pedidos, ${detalleDtos.size} detalles")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    private suspend fun fetchAllPedidoPages(): List<PedidoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("pedidos").select {
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
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<DetallePedidoDto>()
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
