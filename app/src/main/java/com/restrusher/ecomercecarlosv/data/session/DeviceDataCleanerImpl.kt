package com.restrusher.ecomercecarlosv.data.session

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.data.queue.QueueProcessor
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
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
    private val sessionManager: SessionManager,
) : DeviceDataCleaner {

    override suspend fun wipeCachedDataIfFullySynced(): Boolean {
        if (networkMonitor.isOnline) queueProcessor.flush()
        dataSynchronizer.resetStaleness()
        val pending = syncOperationDao.pendingCount()
        if (pending > 0) {
            Log.w(TAG, "keeping cached data — $pending operation(s) still unsynced")
            return false
        }
        wipeCachedDataForNewUser()
        return true
    }

    override suspend fun wipeCachedDataForNewUser() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        dataSynchronizer.resetStaleness()
        sessionManager.releaseDevice()
    }

    override suspend fun pendingWriteCount(): Int = syncOperationDao.pendingCount()

    companion object {
        private const val TAG = "DeviceDataCleaner"
    }
}
