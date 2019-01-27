import java.io.DataInputStream
import java.net.ServerSocket
import java.util.concurrent.CompletableFuture


class Server {

    fun init(): CompletableFuture<Unit> {
        val ss = ServerSocket(4000)
        println("Opening server Socket on ${ss.inetAddress} and port ${ss.localPort}")
        return CompletableFuture.supplyAsync() {
            println("Listening to socket...")
            val s = ss.accept()
            val dis = DataInputStream(s.getInputStream())
            val str = dis.readUTF() as String
            println("message= $str")
            ss.close()
        }
    }
}


