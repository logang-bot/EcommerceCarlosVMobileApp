package com.restrusher.ecomercecarlosv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.data.local.entity.ClienteEntity
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.UserEntity

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE mercados ADD COLUMN mapsUrl TEXT")
        db.execSQL("ALTER TABLE mercados ADD COLUMN latitude REAL")
        db.execSQL("ALTER TABLE mercados ADD COLUMN longitude REAL")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `clientes` (
                `id` TEXT NOT NULL,
                `mercadoId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `photoUrl` TEXT,
                `phones` TEXT NOT NULL,
                `mapsUrl` TEXT,
                `isBlacklisted` INTEGER NOT NULL DEFAULT 0,
                `blacklistReason` TEXT,
                `blacklistedAt` INTEGER,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`mercadoId`) REFERENCES `mercados`(`id`) ON DELETE CASCADE
            )""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_clientes_mercadoId` ON `clientes` (`mercadoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_clientes_name` ON `clientes` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_clientes_isBlacklisted` ON `clientes` (`isBlacklisted`)")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clientes ADD COLUMN blacklistBalance REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `productos` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `price` REAL NOT NULL,
                `photoUrl` TEXT,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_productos_name` ON `productos` (`name`)")
    }
}

@Database(
    entities = [
        MercadoEntity::class,
        UserEntity::class,
        ClienteEntity::class,
        ProductoEntity::class,
    ],
    version = 8,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mercadoDao(): MercadoDao
    abstract fun userDao(): UserDao
    abstract fun clienteDao(): ClienteDao
    abstract fun productoDao(): ProductoDao
}
