package org.bisdk.sdk

import org.bisdk.Logger
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import javax.xml.parsers.DocumentBuilderFactory

data class DiscoveryData(
        val mac: String,
        val sourceAddress: InetAddress,
        val swVersion: String,
        val hwVersion: String,
        val protocol: String
) {
    fun getGatewayId() = mac.replace(":", "").toUpperCase()
}

/**
 * For bisecure gateway discovery, a certain string message is sent over UDP and a result is sent back from the gateway.
 */
class Discovery {

    fun sendDiscoveryRequest() {
        val message = "<Discover target=\"LogicBox\"/>"
        val messageData = message.toByteArray()
        val socket = DatagramSocket()
        socket.broadcast = true
        val request = DatagramPacket(messageData, messageData.size, InetAddress.getByName("255.255.255.255"), 4001)
        socket.send(request)
    }

    fun startServer(): CompletableFuture<DiscoveryData> {
        val serverSocket = DatagramSocket(null)
        serverSocket.reuseAddress = true
        val socketAddress = InetSocketAddress(4002)
        serverSocket.bind(socketAddress)
        val receiveData = ByteArray(1024)
        val receivePacket = DatagramPacket(receiveData, receiveData.size)
        Logger.info("Starting UDP Server on host ${socketAddress.hostString} and port ${serverSocket.localPort}")
        return CompletableFuture.supplyAsync {
            serverSocket.receive(receivePacket)
            val sentence = String(receivePacket.data.copyOf(receivePacket.length))
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val xmlInput = InputSource(StringReader(sentence))
            val doc = dBuilder.parse(xmlInput)
            val element = doc.documentElement
            val data = DiscoveryData(
                element.getAttribute("mac"),
                receivePacket.address,
                element.getAttribute("swVersion"),
                element.getAttribute("hwVersion"),
                element.getAttribute("protocol")
            )
            Logger.info("Gateway data: $data")
            data
        }
    }

    fun start(): CompletableFuture<DiscoveryData> {
        val startServer = startServer()
        sendDiscoveryRequest()
        return startServer
    }

}
