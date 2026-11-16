package com.restrusher.ecomercecarlosv.screenshots.fakes

import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Always reports a connection, so no screen draws an offline state in a portfolio shot. Remote
 * calls are still prevented: [ScreenshotSessionManager] answers `OFFLINE` when a session is checked.
 */
@Singleton
class ScreenshotNetworkMonitor @Inject constructor() : NetworkMonitor {
    override val isOnline: Boolean = true
    override val isOnlineFlow: Flow<Boolean> = flowOf(true)
}
