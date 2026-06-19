package com.restrusher.ecomercecarlosv.domain.repository

import android.net.Uri

interface CleanupRepository {
    suspend fun countPedidosOlderThan(cutoffMs: Long): Int
    suspend fun exportPedidosToFile(
        cutoffMs: Long,
        useXlsx: Boolean,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Triple<String, Long, Uri>
    suspend fun deletePedidosFromCloud(
        cutoffMs: Long,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Int
}
