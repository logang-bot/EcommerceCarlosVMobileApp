package com.restrusher.ecomercecarlosv

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.ImageLoaderFactory
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.restrusher.ecomercecarlosv.data.queue.QueueProcessor
import com.restrusher.ecomercecarlosv.data.queue.SyncNotifier
import com.restrusher.ecomercecarlosv.data.queue.SyncWorker
import com.restrusher.ecomercecarlosv.data.sync.DataSynchronizer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PedidosApp : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var dataSynchronizer: DataSynchronizer
    @Inject lateinit var queueProcessor: QueueProcessor
    @Inject lateinit var syncNotifier: SyncNotifier

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .crossfade(true)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncNotifier.createChannel()
        dataSynchronizer.start()
        queueProcessor.start()
        SyncWorker.schedule(WorkManager.getInstance(this))
    }
}
