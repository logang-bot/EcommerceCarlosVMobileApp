package com.restrusher.ecomercecarlosv.data.session

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.data.queue.QueueProcessor
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDataCleanerImpl @Inject constructor(
    private val queueProcessor: QueueProcessor,
    private val syncOperationDao: SyncOperationDao,
    private val networkMonitor: NetworkMonitor,
    private val dataSynchronizer: DataSynchronizer,
    private val database: AppDatabase,
) : DeviceDataCleaner {

    override suspend fun wipeCachedDataIfFullySynced(): Boolean {
        if (networkMonitor.isOnline) queueProcessor.flush()
        dataSynchronizer.resetStaleness()
        val pending = syncOperationDao.pendingCount()
        if (pending > 0) {
            Log.w(TAG, "keeping cached data — $pending operation(s) still unsynced")
            return false
        }
        withContext(Dispatchers.IO) { database.clearAllTables() }
        return true
    }

    companion object {
        private const val TAG = "DeviceDataCleaner"
    }
}
