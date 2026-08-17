package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.mapper.ClienteMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.ClienteDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncParentResolver
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class ClienteSyncer @Inject constructor(
    private val dao: ClienteDao,
    private val parentResolver: SyncParentResolver,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val dtos = if (since == 0L) {
                fetchAllPages()
            } else {
                supabase.from("clientes").select {
                    filter { gt("updated_at", since) }
                }.decodeList<ClienteDto>()
            }
            val (tombstones, live) = dtos.partition { it.isDeleted }
            // Soft delete, not a row delete: hard-deleting the parent would CASCADE its pedidos away,
            // and the next full pedido sync would re-download them and fail the foreign key.
            tombstones.forEach { dao.softDeleteById(it.id) }
            val mercados = parentResolver.ensureMercadosExist(live.mapTo(mutableSetOf()) { it.mercadoId })
            live.forEach { upsert(it, isMercadoResolved = it.mercadoId in mercados) }
            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${dtos.size} clientes")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    /** Writes are guarded individually so one unwritable row can never abort the whole pull. */
    private suspend fun upsert(dto: ClienteDto, isMercadoResolved: Boolean) {
        if (!isMercadoResolved) {
            Log.w(TAG, "skipping cliente ${dto.id}: mercado ${dto.mercadoId} not available")
            return
        }
        runCatching {
            val entity = ClienteMapper.fromDto(dto, dao.getById(dto.id))
            if (dao.insert(entity) == -1L) dao.update(entity)
        }.onFailure { Log.e(TAG, "skipping cliente ${dto.id}: write failed", it) }
    }

    private suspend fun fetchAllPages(): List<ClienteDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("clientes").select {
                filter { eq("is_deleted", false) }
                // Offset paging without a stable sort can skip rows between pages.
                order("id", Order.ASCENDING)
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<ClienteDto>()
            addAll(page)
            if (page.size < BATCH_SIZE) break
            offset += BATCH_SIZE
        }
    }

    companion object {
        private const val TAG = "ClienteSyncer"
        private const val BATCH_SIZE = 1000
    }
}
