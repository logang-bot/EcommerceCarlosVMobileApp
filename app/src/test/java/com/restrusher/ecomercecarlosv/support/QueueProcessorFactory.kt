package com.restrusher.ecomercecarlosv.support

import androidx.work.WorkManager
import com.restrusher.ecomercecarlosv.data.error.GlobalErrorHandler
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.queue.QueueProcessor
import com.restrusher.ecomercecarlosv.data.remote.StorageService
import com.restrusher.ecomercecarlosv.fakes.FakeNetworkMonitor
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope

/**
 * Builds a [QueueProcessor] over a real Room database.
 *
 * The Supabase client is a relaxed mock and is never asserted on — the tests deliberately drive
 * paths that stop before the network call (see `QueueProcessorFlushTest`), because `from()` is a
 * supabase-kt extension function that cannot be stubbed meaningfully.
 */
fun queueProcessorOver(
    db: AppDatabase,
    scope: CoroutineScope,
    networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
    sessionManager: FakeSessionManager = FakeSessionManager(),
    storageService: StorageService = mockk(relaxed = true),
    errorHandler: GlobalErrorHandler = GlobalErrorHandler(),
): QueueProcessor = QueueProcessor(
    syncOperationDao = db.syncOperationDao(),
    mercadoDao = db.mercadoDao(),
    clienteDao = db.clienteDao(),
    productoDao = db.productoDao(),
    pedidoDao = db.pedidoDao(),
    detalleDao = db.detallePedidoDao(),
    pagoDao = db.pagoDao(),
    umbralesDao = db.umbralesDao(),
    networkMonitor = networkMonitor,
    supabase = mockk<SupabaseClient>(relaxed = true),
    storageService = storageService,
    errorHandler = errorHandler,
    sessionManager = sessionManager,
    workManager = mockk<WorkManager>(relaxed = true),
    appScope = scope,
)
