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

    override suspend fun sync(): SyncResult {
        return runCatching {
            val pedidoDtos = supabase.from("pedidos").select().decodeList<PedidoDto>()
            pedidoDtos.forEach { dto ->
                val entity = PedidoMapper.fromDto(dto)
                pedidoDao.insert(entity)
            }

            val detalleDtos = supabase.from("detalle_pedido").select().decodeList<DetallePedidoDto>()
            if (detalleDtos.isNotEmpty()) {
                detalleDao.insertAll(detalleDtos.map(DetallePedidoMapper::fromDto))
            }

            Log.d(TAG, "sync: fetched ${pedidoDtos.size} pedidos and ${detalleDtos.size} detalles")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync: failed", e)
            SyncResult.Failure(e)
        }
    }

    companion object {
        private const val TAG = "PedidoSyncer"
    }
}
