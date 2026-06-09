package com.restrusher.ecomercecarlosv.di

import android.content.Context
import androidx.room.Room
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_4_5
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_5_6
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_6_7
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_7_8
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_8_9
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_9_10
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
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
        Room.databaseBuilder(context, AppDatabase::class.java, "pedidos_db")
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .build()

    @Provides
    fun provideMercadoDao(db: AppDatabase): MercadoDao = db.mercadoDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideClienteDao(db: AppDatabase): ClienteDao = db.clienteDao()

    @Provides
    fun provideProductoDao(db: AppDatabase): ProductoDao = db.productoDao()

    @Provides
    fun providePedidoDao(db: AppDatabase): PedidoDao = db.pedidoDao()

    @Provides
    fun provideDetallePedidoDao(db: AppDatabase): DetallePedidoDao = db.detallePedidoDao()
}
