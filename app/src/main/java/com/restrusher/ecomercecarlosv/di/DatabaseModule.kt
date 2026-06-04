package com.restrusher.ecomercecarlosv.di

import android.content.Context
import androidx.room.Room
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pedidos_db").build()

    @Provides
    fun provideMercadoDao(db: AppDatabase): MercadoDao = db.mercadoDao()
}
