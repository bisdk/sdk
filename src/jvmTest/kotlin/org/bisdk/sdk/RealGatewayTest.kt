package org.bisdk.sdk

import kotlinx.coroutines.runBlocking
import org.bisdk.Logger
import org.junit.Before
import org.junit.Test

internal class RealGatewayTest {

    // TODO: migrate to unit5 and have conditional test
    // @EnabledIfEnvironmentVariable(named = "bisecureUsername", matches = ".+")
    private var ignoreTests: Boolean = false
    private lateinit var username: String
    private lateinit var password: String

    @Before
    fun setup() {
        username = System.getenv("bisecureUsername") ?: ""
        password = System.getenv("bisecurePassword") ?: ""
        ignoreTests = username.isEmpty()
    }

    /**
     * For this test to work you will need a real GW in your network
     * with the username and password set in your environment variables
     *
     * The startup flow:
     * <ul>
     *     <li>Discovery of gateway</li>
     *     <li>Get Name</li>
     *     <li>Login</li>
     *     <li>Logout</li>
     *  </ul>
     */
    @Test
    fun testStartup()  = runBlocking {
        if (ignoreTests) {
            return@runBlocking
        }

        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        println("Name: " + clientAPI.getName())
        println("Ping: " + clientAPI.ping())
        println("Login in...")
        clientAPI.login(username, password)
        val state = clientAPI.getState()
        println("State: $state")
        val groups = clientAPI.getGroups()
        println("Groups: $groups")
        val transition = clientAPI.getTransition(groups[0].ports[0])
//        clientAPI.setState(groups[0].ports[0])
        clientAPI.logout()

    }

    @Test
    fun testPing()  = runBlocking {
        if (ignoreTests) {
            return@runBlocking
        }
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        var i: Int = 0
        val startTime = System.currentTimeMillis()
        while (i++ < 1000 && clientAPI.ping()) {
            Thread.sleep(1000)
        }
        val timeElapsed = System.currentTimeMillis() - startTime
        Logger.info("Ping succeded for " + timeElapsed + "ms")
    }

    @Test
    fun testGetTransitionTiming() = runBlocking {
        if (ignoreTests) {
            return@runBlocking
        }
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()
        val client = GatewayConnection(address = discoveryData.sourceAddress, gatewayId = discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        clientAPI.login("username", "password")
        val groups = clientAPI.getGroups()
        var i = 0
        val times = mutableListOf<Long>()
        var errors = 0
        val startTime = System.currentTimeMillis()
        while (i++ < 200) {
            val localStartTime = System.currentTimeMillis()
            try {
                clientAPI.getTransition(groups[0].ports[0])
            } catch (e: Exception) {
                errors++
            }
            val localTimeElapsed = System.currentTimeMillis() - localStartTime
            times.add(localTimeElapsed)
        }
        val timeElapsed = System.currentTimeMillis() - startTime
        Logger.info("Total time " + timeElapsed + "ms")
        Logger.info("Times: " + times)
        val under1sec = times.filter { it < 1000 }.count()
        val under2sec = times.filter { it < 2000 }.count()
        val under5sec = times.filter { it < 5000 }.count()
        Logger.info("Under 1s: $under1sec, Under 2s: $under2sec, Under 5s: $under5sec, errors: $errors ")
    }

    @Test
    fun testGetGroupsTiming() = runBlocking {
        if (ignoreTests) {
            return@runBlocking
        }
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()
        val client = GatewayConnection(address = discoveryData.sourceAddress, gatewayId = discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client)
        clientAPI.login("username", "password")
        var i = 0
        val times = mutableListOf<Long>()
        var errors = 0
        val startTime = System.currentTimeMillis()
        while (i++ < 200) {
            val localStartTime = System.currentTimeMillis()
            try {
                clientAPI.getGroups()
            } catch (e: Exception) {
                errors++
            }
            val localTimeElapsed = System.currentTimeMillis() - localStartTime
            times.add(localTimeElapsed)
        }
        val timeElapsed = System.currentTimeMillis() - startTime
        Logger.info("Total time " + timeElapsed + "ms")
        Logger.info("Times: " + times)
        val under1sec = times.filter { it < 1000 }.count()
        val under2sec = times.filter { it < 2000 }.count()
        val under5sec = times.filter { it < 5000 }.count()
        Logger.info("Under 1s: $under1sec, Under 2s: $under2sec, Under 5s: $under5sec, errors: $errors ")
    }

}
