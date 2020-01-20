package org.bisdk.sdk

import org.junit.Ignore
import org.junit.Test

internal class RealGatewayTest {

    /**
     * The startup flow:
     * <ul>
     *     <li>Discovery of gateway</li>
     *     <li>Get Name</li>
     *     <li>Login</li>
     *  </ul>
     */
    @Test
    @Ignore // You need a real GW in your network for this test to work
    fun testStartup() {
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        println("Name: " + clientAPI.getName())
        println("Ping: " + clientAPI.ping())
        println("Login in...")
        clientAPI.login("username", "password")
        val state = clientAPI.getState()
        println("State: $state")
        val groups = clientAPI.getGroups()
        println("Groups: $groups")
        val transition = clientAPI.getTransition(groups[0].ports[0])
//        clientAPI.setState(groups[0].ports[0])
        clientAPI.logout()

    }

    @Test
    @Ignore // You need a real GW in your network for this test to work
    fun testPing() {
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        var i : Int = 0
        val startTime = System.currentTimeMillis()
        while (i++ < 1000 && clientAPI.ping()) {
            Thread.sleep(1000)
        }
        val timeElapsed = System.currentTimeMillis() - startTime
        Logger.info("Ping succeded for " + timeElapsed + "ms")
    }

    @Test
    @Ignore // You need a real GW in your network for this test to work
    fun testGetTransitionTiming() {
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()
        val client = GatewayConnection(address = discoveryData.sourceAddress, gatewayId =  discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        clientAPI.login("username", "password")
        val groups = clientAPI.getGroups()
        var i = 0
        val times = mutableListOf<Long>()
        val startTime = System.currentTimeMillis()
        while (i++ < 200 ) {
            val localStartTime = System.currentTimeMillis()
            clientAPI.getTransition(groups[0].ports[0])
            val localTimeElapsed = System.currentTimeMillis() - localStartTime
            times.add(localTimeElapsed)
        }
        val timeElapsed = System.currentTimeMillis() - startTime
        Logger.info("Total time " + timeElapsed + "ms")
        Logger.info("Times: " + times)
        val under1sec = times.filter { it < 1000 }.count()
        val under2sec = times.filter { it < 2000 }.count()
        val under5sec = times.filter { it < 5000 }.count()
        Logger.info("Under 1s: $under1sec, Under 2s: $under2sec, Under 5s: $under5sec ")
    }

}
