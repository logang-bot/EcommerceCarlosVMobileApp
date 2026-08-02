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
        return when (queueProcessor.flush()) {
            FlushOutcome.COMPLETED -> {
                syncNotifier.notifySuccess()
                Result.success()
            }
            FlushOutcome.FAILED -> {
                syncNotifier.notifyFailure()
                Result.retry()
            }
            // Nothing was attempted — we simply had no session yet. Reschedule quietly rather than
            // telling the user a sync failed, since nothing of theirs did. The dismiss is required:
            // notifyStarted posts an ongoing notification that would otherwise never clear.
            FlushOutcome.DEFERRED -> {
                syncNotifier.dismiss()
                Result.retry()
            }
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
