package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.mapper.ClienteMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.ClienteDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class ClienteSyncer @Inject constructor(
    private val dao: ClienteDao,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(): SyncResult {
        return runCatching {
            val dtos = supabase.from("clientes").select().decodeList<ClienteDto>()
            dtos.forEach { dto ->
                val existing = dao.getById(dto.id)
                val entity = ClienteMapper.fromDto(dto, existing)
                if (dao.insert(entity) == -1L) dao.update(entity)
            }
            Log.d(TAG, "sync: fetched and merged ${dtos.size} clientes")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync: failed", e)
            SyncResult.Failure(e)
        }
    }

    companion object {
        private const val TAG = "ClienteSyncer"
    }
}
