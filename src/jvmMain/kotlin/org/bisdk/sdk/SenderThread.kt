package org.bisdk.sdk

import org.bisdk.encodeToGW

/**
 * A Runnable that sends packets whenever they arrive.
 *
 * Used for non - blocking sending, especially when network errors block sending operations
 */
class SenderThread(val client: Client, private val sendTimeout: Int = 5000) : Runnable {
    private val queue = mutableListOf<TransportContainer>()
    private var running = true
    private var exception: Exception? = null

    public fun send(message: TransportContainer) {
        waitUntilQueueEmpty()
        queue.add(message)
        waitForMessageProcessed(message)
    }

    public fun stop() {
        running = false
    }

    private fun waitForMessageProcessed(message: TransportContainer) {
        Logger.log("waitForMessageProcessed")
        val startTime = System.currentTimeMillis()
        while (queue.contains(message)) {
            val elapsedTime = System.currentTimeMillis() - startTime
//            println("elapsedTime: $elapsedTime")
            if (elapsedTime > sendTimeout) {
                queue.remove(message)
                throw IllegalStateException("Message could not be sent after waiting $sendTimeout ms)")
            }
            Thread.sleep(50)
        }
        if(exception != null) {
            val localException = exception
            exception = null
            throw localException!!
        }
        Logger.log("waitForMessageProcessed finished")
    }

    private fun waitUntilQueueEmpty() {
        Logger.log("waitUntilQueueEmpty")
        val startTime = System.currentTimeMillis()
        while (queue.isNotEmpty()) {
            val elapsedTime = System.currentTimeMillis() - startTime
//            println("elapsedTime: $elapsedTime")
            if (elapsedTime > sendTimeout) {
                throw IllegalStateException("Could not enqueue message, queue is full (after waiting $sendTimeout ms)")
            }
            Thread.sleep(50)
        }
        Logger.log("waitUntilQueueEmpty finished")
    }

    override fun run() {
        while (running) {
            while (queue.isEmpty()) {
                Thread.sleep(50)
            }
            val message = queue[0]
            val messageBytes = message.toByteArray().encodeToGW()
            Logger.log("Sending transport container ${message.toHexString()}")
            try {
                client.dataOut.write(messageBytes)
                client.dataOut.flush()
            } catch (e: Exception) {
                exception = e
            }
            queue.remove(message)
            Logger.log("Sending done")
        }
    }
}
