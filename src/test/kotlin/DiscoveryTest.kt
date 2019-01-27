import org.junit.jupiter.api.Test

internal class DiscoveryTest {

    @Test
    fun sendDiscoveryRequest() {
        Discovery().sendDiscoveryRequest()
    }

    @Test
    fun startServer() {
        val discovery = Discovery()
        val server = discovery.startServer()
        discovery.sendDiscoveryRequest()
        server.join()
    }
}
