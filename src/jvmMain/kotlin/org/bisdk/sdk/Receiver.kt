package org.bisdk.sdk

import kotlinx.io.errors.IOException
import org.bisdk.Command
import org.bisdk.Lengths
import org.bisdk.Logger
import org.bisdk.decodeFromGW
import org.bisdk.toHexStringFromGW
import org.bisdk.waitFor
import java.io.DataInputStream

class Receiver(private val dataIn: DataInputStream, private val readTimeout: Int) : Runnable {
    private val queue = mutableListOf<TransportContainer>()
    private var exception: Exception? = null
    private var running = true

    public fun stop() {
        queue.clear()
        running = false
    }

    suspend fun retrieveAnswer(tag: Int): TransportContainer {
        Logger.debug("Waiting for answer with tag $tag")
        waitFor(readTimeout, {
            (queue.find { message -> message.pack.tag == tag } != null) ||
                    (exception != null) ||
                    !running
        }, "PackageReceived")
        throwExceptionIfNoLongerRunning()
        throwExceptionIfOccurred()
        val message = queue.find { message -> message.pack.tag == tag }!!
        queue.remove(message)
        Logger.debug("Answer for tag $tag: $message")
        return message
    }

    private fun throwExceptionIfNoLongerRunning() {
        if (!running) {
            throw InterruptedException("Receiver got signal to stop -> stopping receiving activities ")
        }
    }

    private fun throwExceptionIfOccurred() {
        if (exception != null) {
            val localException = exception
            exception = null
            throw localException!!
        }
    }

    private fun readIncomingPackages() {
        val bytesRead = ArrayList<Byte>()
        var startTime = System.currentTimeMillis()
        Logger.debug("Reading from socket...")
        while (running) {
            if (dataIn.available() > 0) {
                try {
                    while(dataIn.available() > 0) {
                        bytesRead.add(dataIn.readUnsignedByte().toByte())
                    }
                } catch (e: IOException) {
                    exception = e
                }
                if (bytesRead.size >= Lengths.MINIMUM_CONTAINER_SIZE) {  // Only if it contains sender and receiver address
                    Logger.debug("Checking " + bytesRead.toByteArray().toHexStringFromGW())
                    // Perhaps we have some wrong bytes received from the network, we check if we have a valid response without some of the first bytes
                    for (startPos in 0..(bytesRead.size - Lengths.MINIMUM_CONTAINER_SIZE)) {
//                        Logger.debug("startPos: $startPos, bytesReadSize: ${bytesRead.size}")
                        val ba = bytesRead.subList(startPos, bytesRead.size).toByteArray().decodeFromGW()
                        val tc = TransportContainer.from(ba)
                        if (tc.pack.command == Command.EMPTY) {  // TOO much noise
//                            Logger.debug("Empty / unreadable answer received: " + ba.toHexString())
                        }
                        if (tc.hasCorrectChecksum() && tc.pack.command != Command.EMPTY) {
                            if (startPos > 0) {
                                Logger.debug("Ignoring first $startPos bytes: " + bytesRead.subList(0, startPos).toByteArray().toHexStringFromGW())
                            }
                            Logger.debug("Received: $tc in " + (System.currentTimeMillis() - startTime) + "ms")
                            queue.add(tc)
                            if(startPos > 0 || (tc.length() * 2) < (bytesRead.size - startPos)) {
                                Logger.debug("Remaining bytes found startPos: $startPos, tc.size(): ${tc.length() * 2}, bytesRead.size: ${bytesRead.size} => saving...")
                                val remainingBytes = bytesRead.subList(startPos, tc.length() * 2)
                                bytesRead.clear()
                                bytesRead.addAll(remainingBytes)
                            } else {
                                bytesRead.clear()
                            }
                            startTime = System.currentTimeMillis()
                            break
                        }
                    }
                }
            } else {
                Thread.sleep(100)
            }
        }
        Logger.debug("End of reading Thread!")
    }

    override fun run() {
        readIncomingPackages()
    }

}
