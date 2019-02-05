package de.thomasletsch

import org.junit.Test

class DiscoveryTest {

    @Test
    fun sendDiscoveryRequest() {
        Discovery().sendDiscoveryRequest()
    }

    @Test
    fun startServer() {
        val discovery = Discovery()
        val server = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = server.join()
    }
}
