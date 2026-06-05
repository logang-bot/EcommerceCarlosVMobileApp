package com.restrusher.ecomercecarlosv.di

import com.restrusher.ecomercecarlosv.data.repository.impl.MercadoRepositoryImpl
import com.restrusher.ecomercecarlosv.data.repository.impl.UserRepositoryImpl
import com.restrusher.ecomercecarlosv.data.session.SessionManagerImpl
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.UserRepository
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
}
