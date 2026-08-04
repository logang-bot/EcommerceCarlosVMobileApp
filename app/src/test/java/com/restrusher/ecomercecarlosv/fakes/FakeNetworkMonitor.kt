package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(online: Boolean = true) : NetworkMonitor {

    private val state = MutableStateFlow(online)

    override val isOnline: Boolean get() = state.value

    override val isOnlineFlow: Flow<Boolean> = state

    fun setOnline(online: Boolean) {
        state.value = online
    }
}
