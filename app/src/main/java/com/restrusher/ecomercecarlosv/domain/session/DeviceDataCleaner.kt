package com.restrusher.ecomercecarlosv.domain.session

/**
 * Clears the data this device caches for the signed-in user.
 *
 * Separate from [SessionManager] because forgetting credentials and discarding cached business data
 * are different decisions: a user can sign out and keep their pedidos cached, or forget the device
 * and lose them.
 */
interface DeviceDataCleaner {

    /**
     * Pushes anything still queued, resets sync staleness, and wipes the cached tables — but only
     * if the write queue drained completely. Unsynced pedidos exist nowhere else, so when the push
     * cannot finish (offline, or the server rejected it) the cached data is deliberately kept.
     *
     * @return true if the cached tables were wiped, false if unsynced work forced them to be kept.
     */
    suspend fun wipeCachedDataIfFullySynced(): Boolean
}
