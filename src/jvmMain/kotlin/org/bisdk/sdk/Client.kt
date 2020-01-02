package org.bisdk.sdk

import org.bisdk.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * The client is responsible for sending and receiving messages to and from the bisecure gateway.
 *
 * The sender address used is first "000000000000" but changes as result from login command.
 */
class Client(
    private val address: InetAddress,
    private var sender: String = defaultSender,
    private var receiver: String,
    var token: String = defaultToken,
    private val port: Int = 4000,
    /**
     * Timeout in ms before a send message request is aborted
     */
    private val sendTimeout: Int = 5000,
    /**
     * Timeout in ms before a read request is aborted
     */
    private val readTimeout: Int = 5000,
    private val connectTimeout: Int = 5000
) : AutoCloseable {

    /**
     * Primary cosntructor for non - kotlin clients. Only contains needed params.
     * Both params can be derived from the {@see Discovery} object.
     */
    constructor(address: InetAddress, gatewayId: String): this(address, defaultSender, gatewayId, defaultToken)

    companion object {
        const val defaultToken: String = "00000000"
        const val defaultSender: String = "000000000000"
    }

    var s: Socket = Socket()
    var dataOut: DataOutputStream
    var dataIn: DataInputStream
    var senderThread: SenderThread

    init {
        Logger.info("Connecting to $address:$port")
        s.connect(InetSocketAddress(address, port), connectTimeout)
        dataOut = DataOutputStream(s.getOutputStream())
        dataIn = DataInputStream(s.getInputStream())
        senderThread = SenderThread(this, sendTimeout)
        Thread(senderThread).start()
    }

    fun setTokenOrDefault(token: String?) {
        this.token = token ?: defaultToken
    }

    fun reconnect() {
        Logger.debug("Reconnecting")
        close()
        connect()
    }

    private fun connect() {
        s = Socket()
        Logger.debug("Connecting...")
        s.connect(InetSocketAddress(address, port), connectTimeout)
        Logger.debug("Connected")
        dataOut = DataOutputStream(s.getOutputStream())
        dataIn = DataInputStream(s.getInputStream())
        senderThread = SenderThread(this)
        Thread(senderThread).start()
    }

    /**
     * Can throw a TimeoutException or a SocketException on error
     */
    fun sendMessage(message: BiPackage) {
        if (message.command == Command.LOGIN) {
            // Reset internal token when new login command is issued
            this.token = defaultToken
        }
        val pack = message.copy(token = token)
        Logger.debug("Sending package $pack")
        val tc = TransportContainer(sender, receiver, pack)
        senderThread.send(tc)
    }

    fun readAnswer(): BiPackage {
        val ba = readBytes()
        if (ba.size < Lengths.Companion.ADDRESS_SIZE) {
            Logger.info("No valid answer received: " + ba.toHexString())
            return BiPackage.empty()
        }
        val tc = TransportContainer.from(ba)
        Logger.debug("Received: $tc")
        if (tc.pack.command == Command.LOGIN) {
            Logger.debug("Received answer of LOGIN command => setting senderId and token")
            sender = tc.receiver
            token = tc.pack.payload.toByteArray().toHexString().substring(2)
        }
        if (tc.pack.command == Command.ERROR && tc.pack.getBiError() != null && tc.pack.getBiError() == BiError.PERMISSION_DENIED) {
            Logger.debug("Received PERMISSION_DENIED")
            throw PermissionDeniedException()
        }
        return tc.pack
    }

    private fun readBytes(): ByteArray {
        val bytesRead = ArrayList<Byte>()
        Logger.debug("Reading from socket...")
        // We have to wait for all bytes a really long time. 3sec should hopefully be enough...
        // Otherwise we would need to read all bytes always and check always if we have a complete package
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < (startTime + readTimeout)) {
            while (dataIn.available() > 0) {
                bytesRead.add(dataIn.readUnsignedByte().toByte())
            }
            if (dataIn.available() == 0) {
                Thread.sleep(100)
                if (dataIn.available() == 0 && bytesRead.size > Lengths.Companion.ADDRESS_SIZE) {
                    // No more data available and enough read
                    break;
                }
            }
        }
        val ba = bytesRead.toByteArray().decodeFromGW()
        Logger.debug("Received from socket: " + ba.toHexString())
        return ba
    }

    override fun close() {
        dataOut.close()
        dataIn.close()
        s.close()
        senderThread.stop()
    }

    override fun toString(): String {
        return "Client(address=$address, sender='$sender', receiver='$receiver', token='$token', port=$port, s=$s, dataOut=$dataOut, dataIn=$dataIn)"
    }
}
