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
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_10_11
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_11_12
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_12_13
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_13_14
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_14_15
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_15_16
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_16_17
import com.restrusher.ecomercecarlosv.data.local.MIGRATION_17_18
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PagoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
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
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)
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

    @Provides
    fun provideSyncOperationDao(db: AppDatabase): SyncOperationDao = db.syncOperationDao()

    @Provides
    fun providePagoDao(db: AppDatabase): PagoDao = db.pagoDao()
}
