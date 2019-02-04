package de.thomasletsch

class Startup {

    /**
     * The startup flow:
     * <ul>
     *     <li>Discovery of gateway</li>
     *     <li>Get Name</li>
     *     <li>Login</li>
     *  </ul>
     */
    fun startup() {
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = Client(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client, "thomas", "aaabbbccc")
        println("Name: " + clientAPI.getName())
        println("Ping: " + clientAPI.ping())
        println("Login in...")
        clientAPI.login()
        val state = clientAPI.getState()
        println("State: $state")
        val groups = clientAPI.getGroups()
        println("Groups: $groups")
        val transition = clientAPI.getTransition(groups[0].ports[0])
//        clientAPI.setState(groups[0].ports[0])
        clientAPI.logout()
    }
}
