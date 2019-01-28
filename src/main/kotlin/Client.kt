
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

class Client(
        private val address: InetAddress,
        private val sender: String,
        private val receiver: String
) {

    fun sendMessage(message: Package) {
        val port = 4000
        println("Connecting to $address:$port")
        val s = Socket(address, port)
        val dataOut = DataOutputStream(s.getOutputStream())
        val dis = DataInputStream(s.getInputStream())
        println("Sending message $message")
        val tc = TransportContainer(sender, receiver, message)
        val messageBytes = tc.toByteArray()
        println("Sending message " + messageBytes.joinToString(separator = "") { encodeByte(it.toUByte()) })
        dataOut.write(messageBytes)
        dataOut.flush()
        var bytesRead = 0
        println("Reading from socket...")
        while (dis.available() > 0 || bytesRead < 30) {
            print(encodeInt(dis.readUnsignedByte()))
            bytesRead++
        }
        dataOut.close()
        s.close()
    }
}
