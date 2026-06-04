package com.restrusher.ecomercecarlosv.di

import com.restrusher.ecomercecarlosv.data.repository.impl.MercadoRepositoryImpl
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
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
}
