package org.bisdk.sdk

import org.bisdk.Command
import org.bisdk.Lengths
import org.bisdk.decodeFromGW
import org.bisdk.toHexString
import java.io.DataInputStream

class Receiver(private val dataIn: DataInputStream, private val readTimeout: Int) : Runnable {
    private val queue = mutableListOf<TransportContainer>()

    private var running = true

    public fun stop() {
        running = false
    }

    fun retrieveAnswer(tag: Int): TransportContainer {
        Logger.debug("Waiting for answer with tag $tag")
        waitFor(readTimeout, { queue.find { message -> message.pack.tag == tag } != null }, "PackageReceived")
        val message = queue.find { message -> message.pack.tag == tag }!!
        queue.remove(message)
        Logger.debug("Answer for tag $tag: $message")
        return message
    }

    private fun readAnswer() {
        val ba = readBytes()
        if (ba.isEmpty()) {
            return
        }
        val tc = TransportContainer.from(ba)
        Logger.debug("Received: $tc")
        queue.add(tc)
    }

    private fun readBytes(): ByteArray {
        val bytesRead = ArrayList<Byte>()
        Logger.debug("Reading from socket...")
        // We have to wait for all bytes a really long time. 5sec should hopefully be enough...
        // Otherwise we would need to read all bytes always and check always if we have a complete package
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < (startTime + readTimeout)) {
            while (dataIn.available() > 0) {
                bytesRead.add(dataIn.readUnsignedByte().toByte())
                val tcMinimalLength = Lengths.Companion.ADDRESS_SIZE * 2 + Lengths.CHECKSUM_BYTES
                if (bytesRead.size > tcMinimalLength) {  // Only if it contains sender and receiver address
                    // Perhaps we have some wrong bytes received from the network, we check if we have a valid response without some of the first bytes
                    (0..(bytesRead.size - tcMinimalLength)).forEach {
                        val ba = bytesRead.subList(it, bytesRead.size).toByteArray().decodeFromGW()
                        val tc = TransportContainer.from(ba)
                        if(tc.pack.command != Command.EMPTY) {
//                            Logger.debug("Checking " + ba.toHexString())
                        }
                        if (tc.hasCorrectChecksum() && tc.pack.command != Command.EMPTY ) {
                            Logger.debug("Received from socket: " + ba.toHexString())
                            return ba
                        }
                    }
                }
            }
            if (dataIn.available() == 0) {
                Thread.sleep(100)
            }
        }
        Logger.debug("No correct value received from socket!")
        Logger.debug("Received: " + bytesRead.toByteArray().decodeFromGW().toHexString())

        return ByteArray(0)
    }

    override fun run() {
        while (running) {
            readAnswer()
        }
    }

}
