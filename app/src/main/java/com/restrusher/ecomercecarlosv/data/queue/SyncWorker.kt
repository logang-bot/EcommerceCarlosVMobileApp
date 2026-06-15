package com.restrusher.ecomercecarlosv.data.queue

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncOperationDao: SyncOperationDao,
    private val queueProcessor: QueueProcessor,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Reset retry counts so rows abandoned by previous flush attempts get a fresh chance.
        syncOperationDao.resetAllRetryCount()
        queueProcessor.flush()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "sync_queue_worker"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
