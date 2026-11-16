package com.restrusher.ecomercecarlosv.screenshots.di

import com.restrusher.ecomercecarlosv.data.network.NetworkMonitor
import com.restrusher.ecomercecarlosv.di.NetworkModule
import com.restrusher.ecomercecarlosv.screenshots.fakes.ScreenshotNetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
abstract class TestNetworkModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ScreenshotNetworkMonitor): NetworkMonitor
}
