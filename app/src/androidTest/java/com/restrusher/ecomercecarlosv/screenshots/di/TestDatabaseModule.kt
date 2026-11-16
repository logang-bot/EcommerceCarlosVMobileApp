package com.restrusher.ecomercecarlosv.screenshots.di

import android.content.Context
import androidx.room.Room
import com.restrusher.ecomercecarlosv.data.local.AppDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PagoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.dao.UmbralesDao
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * An in-memory database in place of `pedidos_db`. Every test starts empty and is seeded by
 * `DemoData`, and nothing touches the data of an app installed on the same device.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

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

    @Provides
    fun provideSyncOperationDao(db: AppDatabase): SyncOperationDao = db.syncOperationDao()

    @Provides
    fun providePagoDao(db: AppDatabase): PagoDao = db.pagoDao()

    @Provides
    fun provideUmbralesDao(db: AppDatabase): UmbralesDao = db.umbralesDao()
}
