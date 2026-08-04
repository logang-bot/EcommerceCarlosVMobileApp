package com.restrusher.ecomercecarlosv

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The staging flavor appends `.staging` to the applicationId, so the package name is asserted by
 * prefix rather than equality — otherwise this fails on the default variant.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(appContext.packageName.startsWith("com.restrusher.ecomercecarlosv"))
    }
}
