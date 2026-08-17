package com.restrusher.ecomercecarlosv.data.install

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.restrusher.ecomercecarlosv.fakes.FakeInstallMarker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FreshInstallWiperTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: IsolatedDataDirContext

    private val roomDb: File get() = File(context.databasesDirectory, "pedidos_db")
    private val dataStoreFile: File get() = File(context.filesDirectory, "datastore/app_preferences.preferences_pb")
    private val cachedImage: File get() = File(context.cacheDirectory, "images/foto.jpg")
    private val prefsCount: Int get() = context.prefsDirectory.listFiles()?.size ?: 0

    @Before
    fun setUp() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        context = IsolatedDataDirContext(base, tempFolder.newFolder("appdata"))
        seedRestoredData()
        // Without this the "was it deleted" assertions could pass simply because the seed never
        // wrote anything.
        assertTrue(roomDb.exists())
        assertTrue(dataStoreFile.exists())
        assertTrue(cachedImage.exists())
        assertEquals(2, prefsCount)
    }

    /** Stands in for a data dir planted by a backup restore or a device-to-device transfer. */
    private fun seedRestoredData() {
        write(roomDb, "sqlite")
        write(dataStoreFile, "biometric_refresh_token")
        write(cachedImage, "jpeg-bytes")
        write(File(context.prefsDirectory, "sync_staleness.xml"), "<map/>")
        write(File(context.prefsDirectory, "theme_prefs.xml"), "<map/>")
    }

    private fun write(file: File, content: String) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    private fun wiperWith(marker: FakeInstallMarker) = FreshInstallWiper(context, marker)

    @Test
    fun `wipes restored data when the marker is absent`() {
        val marker = FakeInstallMarker(present = false)

        wiperWith(marker).wipeIfRestored()

        assertFalse("Room db survived", roomDb.exists())
        assertFalse("DataStore survived — the refresh token would still be readable", dataStoreFile.exists())
        assertFalse("cached photo survived", cachedImage.exists())
        assertEquals("shared_prefs was not emptied", 0, prefsCount)
        assertEquals(1, marker.createCount)
    }

    @Test
    fun `leaves everything alone when the marker is present`() {
        val marker = FakeInstallMarker(present = true)

        wiperWith(marker).wipeIfRestored()

        assertTrue(roomDb.exists())
        assertTrue(dataStoreFile.exists())
        assertTrue(cachedImage.exists())
        assertEquals(2, prefsCount)
        assertEquals(0, marker.createCount)
    }

    @Test
    fun `records the marker only after the wipe has finished`() {
        var tokenStillOnDiskWhenMarked = true
        val marker = FakeInstallMarker(present = false)
        marker.onCreate = { tokenStillOnDiskWhenMarked = dataStoreFile.exists() }

        wiperWith(marker).wipeIfRestored()

        assertFalse(
            "marker was recorded before the wipe — a crash in between would bless restored data",
            tokenStillOnDiskWhenMarked,
        )
    }

    @Test
    fun `keeps data on an install that predates the marker`() {
        markAsUpdatedInPlace()
        val marker = FakeInstallMarker(present = false)

        wiperWith(marker).wipeIfRestored()

        assertTrue("an existing user was wiped by the update that ships the marker", roomDb.exists())
        assertTrue(dataStoreFile.exists())
        assertEquals("the marker should still be recorded, so this runs once only", 1, marker.createCount)
    }

    private fun markAsUpdatedInPlace() {
        val info = shadowOf(context.packageManager)
            .getInternalMutablePackageInfo(context.packageName)
        info.firstInstallTime = 1_000L
        info.lastUpdateTime = 2_000L
    }
}
