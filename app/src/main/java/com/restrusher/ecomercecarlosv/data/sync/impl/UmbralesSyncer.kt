package com.restrusher.ecomercecarlosv.data.sync.impl

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.UmbralesDao
import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import com.restrusher.ecomercecarlosv.data.mapper.UmbralesMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.UmbralesDto
import com.restrusher.ecomercecarlosv.data.sync.EntitySyncer
import com.restrusher.ecomercecarlosv.data.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

/**
 * Singleton-row syncer — there is exactly one `umbrales` record (id = "global"), so unlike the
 * other syncers this always fetches that one row rather than paging or filtering by [since].
 */
class UmbralesSyncer @Inject constructor(
    private val dao: UmbralesDao,
    private val supabase: SupabaseClient,
) : EntitySyncer {

    override suspend fun sync(since: Long): SyncResult {
        return runCatching {
            val dto = supabase.from("umbrales")
                .select { filter { eq("id", UmbralesEntity.SINGLETON_ID) } }
                .decodeSingleOrNull<UmbralesDto>()
            if (dto != null) {
                dao.insert(UmbralesMapper.fromDto(dto))
            }
            Log.d(TAG, "sync: umbrales ${if (dto != null) "updated from remote" else "no remote row found"}")
            SyncResult.Success
        }.getOrElse { e ->
            Log.e(TAG, "sync failed", e)
            SyncResult.Failure(e)
        }
    }

    companion object {
        private const val TAG = "UmbralesSyncer"
    }
}
