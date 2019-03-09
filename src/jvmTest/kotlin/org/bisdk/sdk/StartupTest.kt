package org.bisdk.sdk

import org.junit.Ignore
import org.junit.Test

internal class StartupTest {

    @Test
    @Ignore // Doesn't work for everyone
    fun testStartup() {
        Startup().startup()
    }

}
