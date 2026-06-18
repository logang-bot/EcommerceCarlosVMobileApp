package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.mapper.ProductoMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.ProductoDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class ProductoSyncer @Inject constructor(
    private val dao: ProductoDao,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val dtos = if (since == 0L) {
                fetchAllPages()
            } else {
                supabase.from("productos").select {
                    filter { gt("updated_at", since) }
                }.decodeList<ProductoDto>()
            }
            dtos.forEach { dto ->
                dao.insert(ProductoMapper.fromDto(dto))
            }
            Log.d(TAG, "${if (since > 0L) "delta" else "full"} sync: ${dtos.size} productos")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    private suspend fun fetchAllPages(): List<ProductoDto> = buildList {
        var offset = 0L
        while (true) {
            val page = supabase.from("productos").select {
                range(offset, offset + BATCH_SIZE - 1)
            }.decodeList<ProductoDto>()
            addAll(page)
            if (page.size < BATCH_SIZE) break
            offset += BATCH_SIZE
        }
    }

    companion object {
        private const val TAG = "ProductoSyncer"
        private const val BATCH_SIZE = 1000
    }
}
