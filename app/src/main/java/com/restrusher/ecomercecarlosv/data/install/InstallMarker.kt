package com.restrusher.ecomercecarlosv.data.install

/**
 * Records that an installation has been seen by the app before.
 *
 * The marker must live somewhere that an uninstall destroys and that no backup or device-to-device
 * transfer can carry, so that "marker missing but data present" reliably means the data on disk did
 * not come from this installation. See [KeystoreInstallMarker] for why the Android Keystore fits.
 *
 * An interface only so tests can drive [FreshInstallWiper] without a real keystore.
 */
interface InstallMarker {

    /** True when this installation has already been recorded. */
    fun isPresent(): Boolean

    /** Records this installation. Only call once any pending wipe has finished. */
    fun create()
}
