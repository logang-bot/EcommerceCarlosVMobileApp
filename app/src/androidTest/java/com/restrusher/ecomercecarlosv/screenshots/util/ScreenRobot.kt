package com.restrusher.ecomercecarlosv.screenshots.util

import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Drives the running app by what is on screen and saves full-device screenshots.
 *
 * Shots are named `<prefix>_<name>.png` (e.g. `light_03_clientes.png`) and written through the test
 * storage service; AGP copies them to
 * `app/build/outputs/connected_android_test_additional_output/` when the run finishes.
 */
@OptIn(ExperimentalTestApi::class)
class ScreenRobot(private val compose: ComposeTestRule, private val prefix: String) {

    fun capture(name: String) {
        compose.waitForIdle()
        // Lets ripples, the status bar and lazy images settle; Compose idling does not cover them.
        Thread.sleep(SETTLE_MS)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        bitmap.writeToTestStorage("${prefix}_$name")
    }

    fun waitForText(text: String) {
        compose.waitUntilAtLeastOneExists(hasText(text, substring = true), TIMEOUT_MS)
    }

    fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    fun tapText(text: String) = tap(hasText(text, substring = true))

    fun tapText(@StringRes id: Int) = tapText(string(id))

    fun tapDescription(@StringRes id: Int) = tap(hasContentDescription(string(id)))

    /** For rows inside a plain scrolling Column that may sit below the fold. */
    fun scrollToAndTapText(text: String) {
        val clickable = hasText(text, substring = true) and hasClickAction()
        compose.waitUntilAtLeastOneExists(clickable, TIMEOUT_MS)
        val node = compose.onAllNodes(clickable).onFirst()
        node.performScrollTo()
        node.performClick()
    }

    fun longPressText(text: String) {
        waitForText(text)
        compose.onAllNodes(hasText(text) and hasClickAction()).onFirst().performTouchInput { longClick() }
    }

    fun typeIntoFirstField(text: String) {
        compose.onAllNodes(hasSetTextAction()).onFirst().performTextInput(text)
        Espresso.closeSoftKeyboard()
    }

    private fun tap(matcher: SemanticsMatcher) {
        val clickable = matcher and hasClickAction()
        compose.waitUntilAtLeastOneExists(clickable, TIMEOUT_MS)
        compose.onAllNodes(clickable).onFirst().performClick()
        compose.waitForIdle()
    }

    private companion object {
        const val SETTLE_MS = 600L
        const val TIMEOUT_MS = 10_000L
    }
}
