package org.bisdk.sdk

import org.bisdk.Logger
import org.bisdk.encodeToGW
import org.bisdk.waitFor
import java.io.DataOutputStream

/**
 * A Runnable that sends packets whenever they arrive.
 *
 * Used for non - blocking sending, especially when network errors block sending operations
 */
class Sender(private val dataOut: DataOutputStream, private val sendTimeout: Int) : Runnable {
    private val queue = mutableListOf<TransportContainer>()
    private var running = true
    private var exception: Exception? = null

    public suspend fun send(message: TransportContainer) {
        waitFor(sendTimeout, {queue.isEmpty()}, "EmptyQueue")
        queue.add(message)
        waitFor(sendTimeout, {!queue.contains(message)}, "MessageProcessed", {queue.remove(message)})
        throwExceptionIfOccurred()
    }

    public fun stop() {
        running = false
    }

    private fun throwExceptionIfOccurred() {
        if (exception != null) {
            val localException = exception
            exception = null
            throw localException!!
        }
    }

    override fun run() {
        while (running) {
            while (running && queue.isEmpty()) {
                Thread.sleep(50)
            }
            // If we were stopped in the meantime, return here
            if(!running) {
                return
            }
            val message = queue[0]
            val messageBytes = message.toByteArray().encodeToGW()
            Logger.debug("Sending transport container ${message.toHexString()}")
            try {
                dataOut.write(messageBytes)
                dataOut.flush()
            } catch (e: Exception) {
                exception = e
            }
            queue.remove(message)
            Logger.debug("Sending done")
        }
    }
}
