class Startup {

    fun startup() {
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val server = Server()
        val serverFuture = server.init()

        val client = Client(discoveryData.sourceAddress)
        client.sendMessage()
        serverFuture.join()
    }
}
