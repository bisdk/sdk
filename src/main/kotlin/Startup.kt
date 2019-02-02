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
        val clientAPI = ClientAPI(client)
        println("Name: " + clientAPI.getName())
        println("Login in...")
        clientAPI.login("thomas", "aaabbbccc")
        val groups = clientAPI.getGroupsForUser()
        println("Groups: " + groups)
    }
}
