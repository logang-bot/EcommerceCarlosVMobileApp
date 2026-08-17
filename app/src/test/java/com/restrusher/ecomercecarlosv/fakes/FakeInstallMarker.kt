package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.data.install.InstallMarker

class FakeInstallMarker(private var present: Boolean = false) : InstallMarker {

    var createCount = 0
        private set

    /** Lets a test assert what was still on disk at the moment the marker was recorded. */
    var onCreate: () -> Unit = {}

    override fun isPresent(): Boolean = present

    override fun create() {
        createCount++
        present = true
        onCreate()
    }
}
