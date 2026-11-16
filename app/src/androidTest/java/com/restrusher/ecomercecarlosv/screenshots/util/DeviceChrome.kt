package com.restrusher.ecomercecarlosv.screenshots.util

import android.content.res.Configuration
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Device-level setup that makes the shots look staged rather than captured mid-day: SystemUI demo
 * mode (12:00, full battery and signal, no notification icons) and the system night mode, which
 * the app's own theme follows in a few places through `isSystemInDarkTheme()`.
 *
 * Commands run as the shell user through UiAutomation, so no extra permissions are needed.
 * `cmd uimode night` needs API 29+.
 */
object DeviceChrome {

    private const val NIGHT_MODE_TIMEOUT_MS = 5_000L
    private const val POLL_MS = 100L
    private const val DEMO = "am broadcast -a com.android.systemui.demo -e command"

    fun enterDemoMode() {
        shell("settings put global sysui_demo_allowed 1")
        shell("$DEMO enter")
        shell("$DEMO clock -e hhmm 1200")
        shell("$DEMO battery -e level 100 -e plugged false")
        shell("$DEMO network -e wifi show -e level 4 -e fully true")
        shell("$DEMO network -e mobile show -e datatype none -e level 4")
        shell("$DEMO notifications -e visible false")
    }

    fun exitDemoMode() {
        shell("$DEMO exit")
    }

    /** The current setting, e.g. "yes", "no" or "auto", so it can be put back afterwards. */
    fun currentNightMode(): String = shell("cmd uimode night").substringAfter(':').trim()

    fun setNightMode(mode: String) {
        shell("cmd uimode night $mode")
    }

    /**
     * Switches night mode and waits until this process has received the new configuration, so the
     * activity launched next doesn't start in the old mode and get recreated halfway through a flow.
     */
    fun applyNightMode(dark: Boolean) {
        setNightMode(if (dark) "yes" else "no")
        val deadline = System.currentTimeMillis() + NIGHT_MODE_TIMEOUT_MS
        while (isNight() != dark && System.currentTimeMillis() < deadline) Thread.sleep(POLL_MS)
    }

    private fun isNight(): Boolean {
        val uiMode = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.uiMode
        return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    /** Runs [command] and waits for it to finish by draining its output. */
    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes().decodeToString() }
    }
}
