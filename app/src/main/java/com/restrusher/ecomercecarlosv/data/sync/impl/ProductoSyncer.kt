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

    override suspend fun sync(): SyncResult {
        return runCatching {
            val dtos = supabase.from("productos").select().decodeList<ProductoDto>()
            dtos.forEach { dto ->
                dao.insert(ProductoMapper.fromDto(dto))
            }
            Log.d(TAG, "sync: fetched and merged ${dtos.size} productos")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync: failed", e)
            SyncResult.Failure(e)
        }
    }

    companion object {
        private const val TAG = "ProductoSyncer"
    }
}
