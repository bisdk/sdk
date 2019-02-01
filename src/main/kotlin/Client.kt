
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

class Client(
        private val address: InetAddress,
        private var sender: String,
        private var receiver: String,
        private val port: Int = 4000
): AutoCloseable {

    var s: Socket = Socket(address, port)
    var dataOut = DataOutputStream(s.getOutputStream())
    var dataIn = DataInputStream(s.getInputStream())

    init {
        println("Connecting to $address:$port")
    }

    fun reconnect() {
        println("Reconnecting")
        close()
        s = Socket(address, port)
        dataOut = DataOutputStream(s.getOutputStream())
        dataIn = DataInputStream(s.getInputStream())
    }

    fun sendMessage(message: Package) {
        println("Sending message $message")
        val tc = TransportContainer(sender, receiver, message)
        val messageBytes = tc.toByteArray().encodeTC()
        println("Sending transport container ${tc.toHexString()}")
        println("Raw: ${tc.toByteArray().encodeTC().toHexString()}")
        dataOut.write(messageBytes)
        dataOut.flush()
    }

    fun readAnswer(): Package {
        val ba = readBytes()
        val tc = TransportContainer.from(ba)
        println("Received: $tc")
//        sender = tc.receiver
//        receiver = tc.sender
        return tc.pack
    }

    private fun readBytes(): ByteArray {
        val bytesRead = ArrayList<Byte>()
        println("Reading from socket...")
        val timeout = 2000
        val startTime =  System.currentTimeMillis()
        while (dataIn.available() == 0 && System.currentTimeMillis() < (startTime + timeout)) {
            Thread.sleep(100)
        }
        if(dataIn.available() == 0) {
            println("No Data read")
        }
        while (dataIn.available() > 0 && bytesRead.size < 30) {
            bytesRead.add(dataIn.readUnsignedByte().toByte())
        }
        val ba = bytesRead.toByteArray().decodeTC()
        println("Received from socket: " + ba.toHexString())
        return ba
    }

    override fun close() {
        dataOut.close()
        dataIn.close()
        s.close()
    }
}
