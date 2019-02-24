package org.bisdk.sdk

import org.junit.Ignore
import org.junit.Test

class DiscoveryTest {

    @Test
    fun sendDiscoveryRequest() {
        Discovery().sendDiscoveryRequest()
    }

    @Test
    @Ignore // Doesn't work for everyone
    fun startServer() {
        val discovery = Discovery()
        val server = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = server.join()
    }
}
