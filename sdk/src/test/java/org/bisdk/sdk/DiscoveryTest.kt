package org.bisdk.sdk

import org.junit.Ignore
import org.junit.Test

class DiscoveryTest {

    @Test
    fun sendDiscoveryRequest() {
        org.bisdk.sdk.Discovery().sendDiscoveryRequest()
    }

    @Test
    @Ignore // Doesn't work for everyone
    fun startServer() {
        val discovery = org.bisdk.sdk.Discovery()
        val server = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = server.join()
    }
}
