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

    override suspend fun sync(): SyncResult {
        return runCatching {
            val dtos = supabase.from("mercados").select().decodeList<MercadoDto>()
            dtos.forEach { dto ->
                val entity = MercadoMapper.fromDto(dto)
                if (dao.insert(entity) == -1L) dao.update(entity)
            }
            Log.d(TAG, "sync: fetched and merged ${dtos.size} mercados")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync: failed", e)
            SyncResult.Failure(e)
        }
    }

    companion object {
        private const val TAG = "MercadoSyncer"
    }
}
