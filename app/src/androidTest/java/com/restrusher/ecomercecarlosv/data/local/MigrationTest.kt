package com.restrusher.ecomercecarlosv.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Replays every migration in [ALL_MIGRATIONS] against real SQLite and validates the result against
 * the exported schema. The app has no `fallbackToDestructiveMigration`, so a broken migration is a
 * crash on launch for every existing install — this is the only suite that catches that.
 *
 * Instrumented rather than JVM because [MigrationTestHelper] loads the exported schemas from the
 * test APK's assets (registered in `app/build.gradle.kts`) and needs real SQLite file upgrades.
 *
 * Start version 16 is absent on purpose: `app/schemas/…/16.json` was never exported, so
 * `createDatabase(16)` is impossible. The 15 → 19 run still executes `MIGRATION_15_16` end to end;
 * only the intermediate v16 shape goes unvalidated.
 *
 * Method names are underscored rather than the suite's usual backticked sentences: `minSdk = 24`
 * means DEX < 040, which rejects spaces in method names outright at build time.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate_fromV4_reachesTheCurrentSchema() = migrateToLatestFrom(4)

    @Test
    fun migrate_fromV5_reachesTheCurrentSchema() = migrateToLatestFrom(5)

    @Test
    fun migrate_fromV6_reachesTheCurrentSchema() = migrateToLatestFrom(6)

    @Test
    fun migrate_fromV7_reachesTheCurrentSchema() = migrateToLatestFrom(7)

    @Test
    fun migrate_fromV8_reachesTheCurrentSchema() = migrateToLatestFrom(8)

    @Test
    fun migrate_fromV9_reachesTheCurrentSchema() = migrateToLatestFrom(9)

    @Test
    fun migrate_fromV10_reachesTheCurrentSchema() = migrateToLatestFrom(10)

    @Test
    fun migrate_fromV11_reachesTheCurrentSchema() = migrateToLatestFrom(11)

    @Test
    fun migrate_fromV12_reachesTheCurrentSchema() = migrateToLatestFrom(12)

    @Test
    fun migrate_fromV13_reachesTheCurrentSchema() = migrateToLatestFrom(13)

    @Test
    fun migrate_fromV14_reachesTheCurrentSchema() = migrateToLatestFrom(14)

    @Test
    fun migrate_fromV15_reachesTheCurrentSchema() = migrateToLatestFrom(15)

    @Test
    fun migrate_fromV17_reachesTheCurrentSchema() = migrateToLatestFrom(17)

    @Test
    fun migrate_fromV18_reachesTheCurrentSchema() = migrateToLatestFrom(18)

    @Test
    fun allMigrations_everyConsecutivePairFrom4ToDeclaredVersion_isPresent() {
        val covered = ALL_MIGRATIONS.map { it.startVersion to it.endVersion }.toSet()

        val missing = (OLDEST_SUPPORTED_VERSION until DATABASE_VERSION)
            .map { it to it + 1 }
            .filterNot { it in covered }

        assertTrue(missing.isEmpty(), "No migration covers $missing — the app would crash on upgrade")
    }

    @Test
    fun allMigrations_noMigration_skipsOrReversesAVersion() {
        val strays = ALL_MIGRATIONS.filterNot { it.endVersion == it.startVersion + 1 }

        assertEquals(emptyList(), strays.map { "${it.startVersion}->${it.endVersion}" })
    }

    /** Creates the database at [startVersion], then runs the whole chain up to [DATABASE_VERSION]. */
    private fun migrateToLatestFrom(startVersion: Int) {
        helper.createDatabase(TEST_DB, startVersion).close()

        // validateDroppedTables = true also catches a migration that leaves an orphan table behind.
        helper.runMigrationsAndValidate(TEST_DB, DATABASE_VERSION, true, *ALL_MIGRATIONS).close()
    }
}

internal const val TEST_DB = "migration-test"

/** v4 is the oldest schema with a migration path; 1–3 predate `MIGRATION_4_5`. */
internal const val OLDEST_SUPPORTED_VERSION = 4
