package com.restrusher.ecomercecarlosv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE mercados ADD COLUMN mapsUrl TEXT")
        db.execSQL("ALTER TABLE mercados ADD COLUMN latitude REAL")
        db.execSQL("ALTER TABLE mercados ADD COLUMN longitude REAL")
    }
}

@Database(
    entities = [
        MercadoEntity::class,
        UserEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mercadoDao(): MercadoDao
    abstract fun userDao(): UserDao
}
