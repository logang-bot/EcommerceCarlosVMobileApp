package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.mapper.MercadoMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.MercadoDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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
            dtos.forEach { dto ->
                val entity = MercadoMapper.fromDto(dto)
                if (dao.insert(entity) == -1L) dao.update(entity)
            }
            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${dtos.size} mercados")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    private suspend fun fetchAllPages(): List<MercadoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("mercados").select {
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
