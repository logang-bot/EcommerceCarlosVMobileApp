package com.restrusher.ecomercecarlosv.di

import com.restrusher.ecomercecarlosv.data.repository.impl.CleanupRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.ClienteRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.MercadoRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.PedidoRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.ProductoRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.UmbralesRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.UserRepositoryImpl
import com.restrusher.ecomercecarlosv.data.session.DeviceDataCleanerImpl
import com.restrusher.ecomercecarlosv.data.session.SessionManagerImpl
import com.restrusher.ecomercecarlosv.domain.repository.CleanupRepository
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.domain.repository.ProductoRepository
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMercadoRepository(impl: MercadoRepositoryImpl): MercadoRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @Binds
    @Singleton
    abstract fun bindDeviceDataCleaner(impl: DeviceDataCleanerImpl): DeviceDataCleaner

    @Binds
    @Singleton
    abstract fun bindClienteRepository(impl: ClienteRepositoryImpl): ClienteRepository

    @Binds
    @Singleton
    abstract fun bindProductoRepository(impl: ProductoRepositoryImpl): ProductoRepository

    @Binds
    @Singleton
    abstract fun bindPedidoRepository(impl: PedidoRepositoryImpl): PedidoRepository

    @Binds
    @Singleton
    abstract fun bindCleanupRepository(impl: CleanupRepositoryImpl): CleanupRepository

    @Binds
    @Singleton
    abstract fun bindUmbralesRepository(impl: UmbralesRepositoryImpl): UmbralesRepository
}
