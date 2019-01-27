
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

class Client(
        val address: InetAddress
) {

    fun sendMessage() {
        val port = 4000
        println("Connecting to $address:$port")
        val s = Socket(address, port)
        val dout = DataOutputStream(s.getOutputStream())
        val dis = DataInputStream(s.getInputStream())
        println("Writing test string...")
        dout.writeUTF("Hello Server")
        dout.flush()
        println("Reading from socket...")
        val str = dis.readUTF() as String
        println("message= $str")
        dout.close()
        s.close()
    }
}
