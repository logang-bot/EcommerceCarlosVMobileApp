package com.restrusher.ecomercecarlosv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.DetallePedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PagoDao
import com.restrusher.ecomercecarlosv.data.local.dao.PedidoDao
import com.restrusher.ecomercecarlosv.data.local.dao.ProductoDao
import com.restrusher.ecomercecarlosv.data.local.dao.SyncOperationDao
import com.restrusher.ecomercecarlosv.data.local.dao.UserDao
import com.restrusher.ecomercecarlosv.data.local.entity.ClienteEntity
import com.restrusher.ecomercecarlosv.data.local.entity.DetallePedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.MercadoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PagoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.PedidoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.ProductoEntity
import com.restrusher.ecomercecarlosv.data.local.entity.SyncOperationEntity
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

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `pedidos` (
                `id` TEXT NOT NULL,
                `clienteId` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `total` REAL NOT NULL,
                `paid` REAL NOT NULL,
                `notes` TEXT,
                `createdAt` INTEGER NOT NULL,
                `paidAt` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`clienteId`) REFERENCES `clientes`(`id`) ON DELETE CASCADE
            )""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pedidos_clienteId` ON `pedidos` (`clienteId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pedidos_status` ON `pedidos` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pedidos_createdAt` ON `pedidos` (`createdAt`)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `detalle_pedido` (
                `id` TEXT NOT NULL,
                `pedidoId` TEXT NOT NULL,
                `productoId` TEXT NOT NULL,
                `productName` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `unitPrice` REAL NOT NULL,
                `catalogPrice` REAL NOT NULL,
                `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`pedidoId`) REFERENCES `pedidos`(`id`) ON DELETE CASCADE
            )""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_detalle_pedido_pedidoId` ON `detalle_pedido` (`pedidoId`)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pedidos ADD COLUMN isSaldoExtra INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pedidos ADD COLUMN itemCount INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clientes ADD COLUMN blacklistIsManualAmount INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clientes ADD COLUMN primaryPhoneIndex INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `sync_operations` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `entityType` TEXT NOT NULL,
                `entityId` TEXT NOT NULL,
                `operation` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `retryCount` INTEGER NOT NULL DEFAULT 0
            )""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_entityType` ON `sync_operations` (`entityType`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_entityId` ON `sync_operations` (`entityId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_createdAt` ON `sync_operations` (`createdAt`)")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE sync_operations ADD COLUMN entityLabel TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE mercados ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE mercados SET updatedAt = createdAt")
        db.execSQL("ALTER TABLE clientes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE clientes SET updatedAt = createdAt")
        db.execSQL("ALTER TABLE productos ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE productos SET updatedAt = createdAt")
        db.execSQL("ALTER TABLE pedidos ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE pedidos SET updatedAt = createdAt")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE mercados ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE clientes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE productos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE pedidos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `pagos` (
                `id` TEXT NOT NULL,
                `pedidoId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `paidAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`pedidoId`) REFERENCES `pedidos`(`id`) ON DELETE CASCADE
            )""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pagos_pedidoId` ON `pagos` (`pedidoId`)")
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
        PedidoEntity::class,
        DetallePedidoEntity::class,
        SyncOperationEntity::class,
        PagoEntity::class,
    ],
    version = 18,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mercadoDao(): MercadoDao
    abstract fun userDao(): UserDao
    abstract fun clienteDao(): ClienteDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun detallePedidoDao(): DetallePedidoDao
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun pagoDao(): PagoDao
}
