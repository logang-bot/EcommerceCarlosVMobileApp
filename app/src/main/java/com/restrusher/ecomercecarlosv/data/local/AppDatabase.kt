package com.restrusher.ecomercecarlosv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity

@Database(
    entities = [
        MercadoEntity::class,
        UserEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mercadoDao(): MercadoDao
    abstract fun userDao(): UserDao
}
