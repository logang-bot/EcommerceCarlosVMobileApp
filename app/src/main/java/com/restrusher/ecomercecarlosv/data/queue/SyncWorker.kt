package com.restrusher.ecomercecarlosv.data.queue

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val queueProcessor: QueueProcessor,
    private val syncNotifier: SyncNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        syncNotifier.notifyStarted()
        val anyFailed = queueProcessor.flush()
        return if (anyFailed) {
            syncNotifier.notifyFailure()
            Result.retry()
        } else {
            syncNotifier.notifySuccess()
            Result.success()
        }
    }

    companion object {
        private const val WORK_NAME = "sync_queue_worker"

        fun schedule(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }
}
