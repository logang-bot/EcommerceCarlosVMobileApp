package com.restrusher.ecomercecarlosv.data.install

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import java.io.File

/**
 * Points every directory [FreshInstallWiper] touches at [root] instead of the real app data dir.
 *
 * Robolectric shares one data dir across all test classes in a JVM, so letting the wiper loose on
 * it deletes `shared_prefs` and `cacheDir` out from under every test that runs afterwards.
 */
class IsolatedDataDirContext(base: Context, private val root: File) : ContextWrapper(base) {

    val filesDirectory = File(root, "files")
    val cacheDirectory = File(root, "cache")
    val prefsDirectory = File(root, "shared_prefs")
    val databasesDirectory = File(root, "databases")

    init {
        listOf(filesDirectory, cacheDirectory, prefsDirectory, databasesDirectory).forEach { it.mkdirs() }
    }

    override fun getFilesDir(): File = filesDirectory

    override fun getCacheDir(): File = cacheDirectory

    override fun getApplicationInfo(): ApplicationInfo =
        ApplicationInfo(super.getApplicationInfo()).apply { dataDir = root.absolutePath }

    override fun deleteDatabase(name: String): Boolean = File(databasesDirectory, name).delete()
}
