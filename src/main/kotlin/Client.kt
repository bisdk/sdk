
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

class Client(
        private val address: InetAddress,
        private val sender: String,
        private val receiver: String,
        private val port: Int = 4000
) {

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
        val messageBytes = tc.toByteArray()
        println("Sending message " + messageBytes.joinToString(separator = "") { encodeByte(it.toUByte()) })
        dataOut.write(messageBytes)
        dataOut.flush()
    }

    fun readBytes(): Int {
        var bytesRead = 0
        println("Reading from socket...")
        val timeout = 2000
        val startTime =  System.currentTimeMillis()
        while (dataIn.available() == 0 && System.currentTimeMillis() < (startTime + timeout)) {
            Thread.sleep(100)
        }
        if(dataIn.available() == 0) {
            println("No Data read")
        }
        while (dataIn.available() > 0 && bytesRead < 30) {
            print(encodeInt(dataIn.readUnsignedByte()))
            bytesRead++
        }
        return bytesRead
    }

    fun close() {
        dataOut.close()
        dataIn.close()
        s.close()
    }
}
