package com.restrusher.ecomercecarlosv.screenshots

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Runs every instrumented test under [HiltTestApplication] so Hilt tests can replace modules.
 *
 * `PedidosApp` normally initializes WorkManager itself (the manifest removes the default
 * initializer). The test application does not, and `WorkerModule` would then throw on
 * `WorkManager.getInstance`, so a synchronous test WorkManager is installed here instead.
 * Skipping `PedidosApp.onCreate` also keeps the sync machinery (DataSynchronizer, QueueProcessor,
 * SyncWorker) from starting, so no test makes a network call on its own.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)

    override fun onStart() {
        WorkManagerTestInitHelper.initializeTestWorkManager(targetContext)
        super.onStart()
    }
}
