package com.restrusher.ecomercecarlosv.data.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Boolean
    val isOnlineFlow: Flow<Boolean>
}
