package com.restrusher.ecomercecarlosv.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * The two migrations that do more than reshape tables — they move data. `MigrationTest`'s sweep
 * proves the resulting *schema* is right; only reading a row back proves the *data* is.
 */
@RunWith(AndroidJUnit4::class)
class MigrationDataTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migration15To16_rowPredatingUpdatedAt_backfillsItFromCreatedAt() {
        val createdAt = 1_700_000_000_000L
        helper.createDatabase(TEST_DB, 15).use { db ->
            db.execSQL(
                "INSERT INTO mercados (id, name, address, createdAt) VALUES (?, ?, ?, ?)",
                arrayOf<Any>("m1", "Mercado de Coche", "Av. Principal", createdAt),
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, DATABASE_VERSION, true, *ALL_MIGRATIONS)

        // Without the backfill these rows would sync as if last touched at epoch 0.
        migrated.use { assertEquals(createdAt, it.selectLong("SELECT updatedAt FROM mercados WHERE id = 'm1'")) }
    }

    @Test
    fun migration18To19_existingInstall_seedsGlobalUmbralesRowWithOldDefaults() {
        helper.createDatabase(TEST_DB, 18).close()

        val migrated = helper.runMigrationsAndValidate(TEST_DB, DATABASE_VERSION, true, *ALL_MIGRATIONS)

        migrated.use { db ->
            assertEquals(1L, db.selectLong("SELECT COUNT(*) FROM umbrales"))
            // The pre-Phase-12 SharedPreferences defaults, kept until the next sync pulls the real values.
            assertEquals(200.0, db.selectDouble("SELECT montoMaximo FROM umbrales WHERE id = 'global'"))
            assertEquals(30L, db.selectLong("SELECT diasMaximos FROM umbrales WHERE id = 'global'"))
        }
    }

    private fun SupportSQLiteDatabase.selectLong(sql: String): Long =
        query(sql).use { it.moveToFirst(); it.getLong(0) }

    private fun SupportSQLiteDatabase.selectDouble(sql: String): Double =
        query(sql).use { it.moveToFirst(); it.getDouble(0) }
}
