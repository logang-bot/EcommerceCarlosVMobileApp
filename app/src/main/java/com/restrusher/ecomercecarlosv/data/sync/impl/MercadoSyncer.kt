package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.mapper.MercadoMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.MercadoDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class MercadoSyncer @Inject constructor(
    private val dao: MercadoDao,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val dtos = if (since == 0L) {
                fetchAllPages()
            } else {
                supabase.from("mercados").select {
                    filter { gt("updated_at", since) }
                }.decodeList<MercadoDto>()
            }
            val (tombstones, live) = dtos.partition { it.isDeleted }
            // Soft delete, not a row delete: hard-deleting the parent would CASCADE its clientes
            // away, and the next full cliente sync would re-download them and fail the foreign key.
            tombstones.forEach { dao.softDeleteById(it.id) }
            live.forEach { upsert(it) }
            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${dtos.size} mercados")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    /** Writes are guarded individually so one unwritable row can never abort the whole pull. */
    private suspend fun upsert(dto: MercadoDto) {
        runCatching {
            val entity = MercadoMapper.fromDto(dto)
            if (dao.insert(entity) == -1L) dao.update(entity)
        }.onFailure { Log.e(TAG, "skipping mercado ${dto.id}: write failed", it) }
    }

    private suspend fun fetchAllPages(): List<MercadoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("mercados").select {
                filter { eq("is_deleted", false) }
                // Offset paging without a stable sort can skip rows between pages.
                order("id", Order.ASCENDING)
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<MercadoDto>()
            addAll(page)
            if (page.size < BATCH_SIZE) break
            offset += BATCH_SIZE
        }
    }

    companion object {
        private const val TAG = "MercadoSyncer"
        private const val BATCH_SIZE = 1000
    }
}
