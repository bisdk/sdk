package org.bisdk.sdk

import org.bisdk.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

/**
 * The client is responsible for sending and receiving messages to and from the bisecure gateway.
 *
 * The sender address used is first "000000000000" but changes as result from login command.
 */
class Client(
    private val address: InetAddress,
    private var sender: String,
    private var receiver: String,
    var token: String = defaultToken,
    private val port: Int = 4000
) : AutoCloseable {

    companion object {
        const val defaultToken: String = "00000000"
    }

    var s: Socket = Socket(address, port)
    var dataOut = DataOutputStream(s.getOutputStream())
    var dataIn = DataInputStream(s.getInputStream())

    init {
        println("Connecting to $address:$port")
    }

    fun setTokenOrDefault(token: String?) {
        this.token = token ?: defaultToken
    }

    fun reconnect() {
        println("Reconnecting")
        close()
        s = Socket(address, port)
        dataOut = DataOutputStream(s.getOutputStream())
        dataIn = DataInputStream(s.getInputStream())
    }

    fun sendMessage(message: BiPackage) {
        if(message.command == Command.LOGIN) {
            // Reset internal token when new login command is issued
            this.token = defaultToken
        }
        val pack = message.copy(token = token)
        println("Sending package $pack")
        val tc = TransportContainer(sender, receiver, pack)
        val messageBytes = tc.toByteArray().encodeToGW()
        println("Sending transport container ${tc.toHexString()}")
//        println("Raw: ${tc.toByteArray().encodeToGW().toHexString()}")
        dataOut.write(messageBytes)
        dataOut.flush()
    }

    fun readAnswer(): BiPackage {
        val ba = readBytes()
        if (ba.size < Lengths.Companion.ADDRESS_SIZE) {
            println("No valid answer received: " + ba.toHexString())
            return BiPackage.empty()
        }
        val tc = TransportContainer.from(ba)
        println("Received: $tc")
        if (tc.pack.command == Command.LOGIN) {
            println("Received answer of LOGIN command => setting senderId and token")
            sender = tc.receiver
            token = tc.pack.payload.toByteArray().toHexString().substring(2)
        }
        if (tc.pack.command == Command.ERROR && tc.pack.getBiError() != null && tc.pack.getBiError() == BiError.PERMISSION_DENIED) {
            println("Received PERMISSION_DENIED")
            throw PermissionDeniedException()
        }
        return tc.pack
    }

    private fun readBytes(): ByteArray {
        val bytesRead = ArrayList<Byte>()
//        println("Reading from socket...")
        // We have to wait for all bytes a really long time. 3sec should hopefully be enough...
        // Otherwise we would need to read all bytes always and check always if we have a complete package
        val timeout = 5000
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < (startTime + timeout)) {
            while (dataIn.available() > 0) {
                bytesRead.add(dataIn.readUnsignedByte().toByte())
            }
            if (dataIn.available() == 0) {
                Thread.sleep(100)
                if(dataIn.available() == 0 && bytesRead.size > Lengths.Companion.ADDRESS_SIZE) {
                    // No more data available and enough read
                    break;
                }
            }
        }
        val ba = bytesRead.toByteArray().decodeFromGW()
        println("Received from socket: " + ba.toHexString())
        return ba
    }

    override fun close() {
        dataOut.close()
        dataIn.close()
        s.close()
    }

    override fun toString(): String {
        return "Client(address=$address, sender='$sender', receiver='$receiver', token='$token', port=$port, s=$s, dataOut=$dataOut, dataIn=$dataIn)"
    }
}
