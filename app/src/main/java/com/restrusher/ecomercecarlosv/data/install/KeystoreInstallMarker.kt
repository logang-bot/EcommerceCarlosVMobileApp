package com.restrusher.ecomercecarlosv.data.install

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.File
import java.security.KeyStore
import javax.crypto.KeyGenerator

/**
 * [InstallMarker] backed by an Android Keystore alias.
 *
 * A keystore alias is the one piece of state that is destroyed on uninstall and that no backup or
 * transfer mechanism can copy — the key material never leaves the TEE. Its mere existence is the
 * signal; the key is never used to encrypt anything.
 *
 * Constructed by hand from `Application.attachBaseContext`, so it must not depend on Hilt.
 */
class KeystoreInstallMarker(private val context: Context) : InstallMarker {

    /**
     * Fails **open** — an unreadable keystore reports "already recorded" so no wipe runs.
     * Keystore access is flaky on a handful of OEM builds, and wiping on every cold start would
     * destroy unsynced queue operations. Disabling backup in the manifest is the primary control;
     * this marker is only the second line of defence, so the safe failure is to do nothing.
     */
    override fun isPresent(): Boolean = runCatching {
        loadKeystore().containsAlias(ALIAS) || fallbackFile().exists()
    }.getOrElse { error ->
        Log.w(TAG, "keystore unreadable — assuming install is already recorded", error)
        true
    }

    override fun create() {
        runCatching { generateKey() }.onFailure { error ->
            Log.w(TAG, "could not create keystore alias — falling back to a file marker", error)
            runCatching { fallbackFile().createNewFile() }
        }
    }

    private fun loadKeystore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun generateKey() {
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        // Deliberately no setUserAuthenticationRequired / setInvalidatedByBiometricEnrollment:
        // the marker is read before any login, and a key invalidated by a fingerprint enrolment
        // change would trigger a spurious wipe of the user's data.
        val spec = KeyGenParameterSpec.Builder(ALIAS, purposes)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    /**
     * Weaker stand-in for devices whose keystore rejects key generation. `noBackupFilesDir` is
     * excluded from backup and transfer by the platform and is removed on uninstall, so it still
     * answers the question correctly — it is just forgeable by anyone with root or `run-as`.
     */
    private fun fallbackFile() = File(context.noBackupFilesDir, ALIAS)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"

        // Versioned so the marker can be rotated later without every install reading as a restore.
        // Not flavour-qualified: staging and production run as different UIDs and so already have
        // separate keystore namespaces.
        const val ALIAS = "install_marker_v1"
        const val TAG = "KeystoreInstallMarker"
    }
}
