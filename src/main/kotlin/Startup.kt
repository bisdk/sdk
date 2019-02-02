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
        // should read 5410EC03615000000000000600090153BA3FD391BAB9
        client.sendMessage(Package(Command.GET_NAME))
        var answer = client.readAnswer()
        println("Answer: " + answer)
        client.sendMessage(Package(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc")))
        answer = client.readAnswer()
        println("Answer: " + answer)
    }
}
