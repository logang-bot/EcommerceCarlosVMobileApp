package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.domain.session.DeviceDataCleaner

class FakeDeviceDataCleaner : DeviceDataCleaner {

    /** How many writes are still queued — what a wipe would destroy. */
    var pendingWrites = 0

    /** What [wipeCachedDataIfFullySynced] reports: false models a queue that would not drain. */
    var wipeIfSyncedResult = true

    var wipeIfFullySyncedCount = 0
        private set
    var wipeForNewUserCount = 0
        private set

    override suspend fun wipeCachedDataIfFullySynced(): Boolean {
        wipeIfFullySyncedCount++
        return wipeIfSyncedResult
    }

    override suspend fun wipeCachedDataForNewUser() {
        wipeForNewUserCount++
    }

    override suspend fun pendingWriteCount(): Int = pendingWrites
}
