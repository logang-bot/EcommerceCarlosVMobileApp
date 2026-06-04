package com.restrusher.ecomercecarlosv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity

@Database(
    entities = [MercadoEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mercadoDao(): MercadoDao
}
