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

    /**
     * Wipes the cached tables outright, discarding anything still queued, and forgets the device
     * owner. For a device changing hands: the incoming user must not inherit the previous one's
     * data, and the queue must not be flushed first — those writes belong to the previous user and
     * pushing them under the new session would file them under the wrong account.
     */
    suspend fun wipeCachedDataForNewUser()

    /** How many writes are still waiting to reach the server, i.e. what a wipe would destroy. */
    suspend fun pendingWriteCount(): Int
}
