package com.restrusher.ecomercecarlosv.data.install

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Deletes local data that arrived from a backup restore or a device-to-device transfer.
 *
 * Disabling backup in the manifest is the primary control; this is the independent second line of
 * defence, so that the guarantee does not rest on one attribute that a template refresh could flip
 * back. If [InstallMarker] is absent while data is on disk, that data did not come from this
 * installation and must not be trusted — it may carry a live Supabase refresh token.
 *
 * **Must run from `Application.attachBaseContext`, synchronously.** By the time Hilt has injected
 * `PedidosApp`'s fields, `SupabaseClient` has already loaded the session from DataStore and
 * `DataSynchronizer`/`ThemeManager` have read their preferences in property initialisers. Deleting
 * a file underneath an open DataStore does not clear its in-memory snapshot, which would then be
 * flushed back to disk — resurrecting the very token this class exists to destroy.
 */
class FreshInstallWiper(
    private val context: Context,
    private val marker: InstallMarker,
) {

    fun wipeIfRestored() {
        if (marker.isPresent()) return
        if (isInPlaceUpgrade()) {
            Log.i(TAG, "install predates the marker — recording it, leaving data alone")
            marker.create()
            return
        }
        wipe()
        // Only after the wipe: marking first would permanently bless restored data if the wipe
        // then crashed, since the next launch would see the marker and skip straight past.
        marker.create()
    }

    /**
     * True when this install has been updated in place at least once, which a restore-then-launch
     * never has. Covers installs that predate the marker, which would otherwise all read as
     * restores and lose their cached data and unsynced queue on the update that ships this.
     */
    private fun isInPlaceUpgrade(): Boolean = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.firstInstallTime != info.lastUpdateTime
    }.getOrDefault(false)

    private fun wipe() {
        Log.w(TAG, "install marker absent — wiping data left behind by a restore or transfer")
        guard("room") { context.deleteDatabase(ROOM_DB) }
        guard("workmanager") { context.deleteDatabase(WORK_DB) }
        guard("datastore") { File(context.filesDir, DATASTORE_DIR).deleteRecursively() }
        guard("prefs") { deleteChildren(File(context.applicationInfo.dataDir, PREFS_DIR)) }
        guard("cache") { deleteChildren(context.cacheDir) }
    }

    /** Keeps one failing step from aborting the rest; a partial wipe still beats none. */
    private fun guard(step: String, block: () -> Unit) {
        runCatching(block).onFailure { Log.w(TAG, "could not wipe $step", it) }
    }

    private fun deleteChildren(dir: File) {
        dir.listFiles()?.forEach { it.deleteRecursively() }
    }

    private companion object {
        const val ROOM_DB = "pedidos_db"

        // Safe to delete here: WorkManager's auto-init is disabled in the manifest and the first
        // getInstance() call happens later, in onCreate. A restored workdb would otherwise carry
        // WorkSpec rows and JobScheduler ids from another install. SyncWorker.schedule() re-enqueues
        // on every start with ExistingWorkPolicy.KEEP, so nothing is lost.
        const val WORK_DB = "androidx.work.workdb"

        const val DATASTORE_DIR = "datastore"

        // Wiped wholesale rather than by name. An allowlist rots the moment someone adds a new
        // preferences file — precisely the failure that caused this bug. theme_prefs goes with it;
        // resetting the theme on a fresh install is correct behaviour anyway.
        const val PREFS_DIR = "shared_prefs"

        const val TAG = "FreshInstallWiper"
    }
}
