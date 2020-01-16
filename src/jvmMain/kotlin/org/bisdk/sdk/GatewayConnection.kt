package org.bisdk.sdk

import org.bisdk.Command
import org.bisdk.toHexString
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * The GatewayConnection is responsible for sending and receiving messages to and from the bisecure gateway.
 *
 * The sender address used is first "000000000000" but changes as result from login command.
 */
class GatewayConnection(
    private val address: InetAddress,
    private var senderId: String = defaultSenderId,
    private var receiverId: String,
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
    /**
     * Timeout in ms before a connection request is aborted
     */
    private val connectTimeout: Int = 5000
) : AutoCloseable {

    /**
     * Primary constructor for non - kotlin clients. Only contains needed params.
     * Both params can be derived from the {@see Discovery} object.
     */
    constructor(address: InetAddress, gatewayId: String) : this(address, defaultSenderId, gatewayId, defaultToken)

    companion object {
        const val defaultToken: String = "00000000"
        const val defaultSenderId: String = "000000000000"
    }

    var s: Socket = Socket()
    var dataOut: DataOutputStream
    var dataIn: DataInputStream
    var sender: Sender
    var receiver: Receiver

    init {
        Logger.info("Connecting to $address:$port")
        s.connect(InetSocketAddress(address, port), connectTimeout)
        dataOut = DataOutputStream(s.getOutputStream())
        dataIn = DataInputStream(s.getInputStream())
        sender = Sender(dataOut, sendTimeout)
        Thread(sender).start()
        receiver = Receiver(dataIn, readTimeout)
        Thread(receiver).start()
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
        sender = Sender(this.dataOut, sendTimeout)
        Thread(sender).start()
        receiver = Receiver(dataIn, readTimeout)
        Thread(receiver).start()
    }

    /**
     * Can throw a TimeoutException or a SocketException on error
     */
    fun sendMessage(message: BiPackage) {
        if (message.command == Command.LOGIN) {
            // Reset internal token when new login command is issued
            this.token = defaultToken
        }
        if (token == defaultToken && message.command != Command.LOGIN && message.command != Command.GET_NAME) {
            Logger.warn("Message to be sent, but not authenticated! Ignoring message...")
            return
        }
        val pack = message.copy(token = token)
        Logger.debug("Sending package $pack")
        val tc = TransportContainer.create(senderId, receiverId, pack)
        sender.send(tc)
    }

    fun readAnswer(tag: Int): TransportContainer {
        val tc = receiver.retrieveAnswer(tag)
        Logger.debug("Received: $tc")
        if (tc.pack.command == Command.LOGIN) {
            Logger.debug("Received answer of LOGIN command => setting senderId and token")
            senderId = tc.receiver
            token = tc.pack.payload.toByteArray().toHexString().substring(2)
        }
        return tc
    }

    override fun close() {
        dataOut.close()
        dataIn.close()
        s.close()
        sender.stop()
        receiver.stop()
    }

    override fun toString(): String {
        return "Client(address=$address, senderId='$senderId', receiverId='$receiverId', token='$token', port=$port, s=$s, dataOut=$dataOut, dataIn=$dataIn)"
    }
}
