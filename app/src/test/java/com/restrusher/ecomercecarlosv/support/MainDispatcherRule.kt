package com.restrusher.ecomercecarlosv.support

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * `viewModelScope` is hard-wired to `Dispatchers.Main.immediate`, which has no implementation on a
 * plain JVM test. Swapping in a [TestDispatcher] is what makes ViewModels constructible at all.
 *
 * Defaults to [UnconfinedTestDispatcher] so `init { viewModelScope.launch { … } }` has already run
 * by the time a test reads state. Pass a `StandardTestDispatcher` when a test needs to observe an
 * intermediate loading state before it settles.
 */
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
