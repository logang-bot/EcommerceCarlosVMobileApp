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

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val dtos = if (since == 0L) {
                fetchAllPages()
            } else {
                supabase.from("clientes").select {
                    filter { gt("updated_at", since) }
                }.decodeList<ClienteDto>()
            }
            dtos.forEach { dto ->
                if (dto.isDeleted) {
                    dao.deleteById(dto.id)
                } else {
                    val existing = dao.getById(dto.id)
                    val entity = ClienteMapper.fromDto(dto, existing)
                    if (dao.insert(entity) == -1L) dao.update(entity)
                }
            }
            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${dtos.size} clientes")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    private suspend fun fetchAllPages(): List<ClienteDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("clientes").select {
                filter { eq("is_deleted", false) }
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
