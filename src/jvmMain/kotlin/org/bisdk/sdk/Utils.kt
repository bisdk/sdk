package org.bisdk.sdk

/**
 * Waits for the given condition to become true. Checks every 50ms if the condition is true.
 * If senderTimeout has exceeded the errorAction is executed (if exists) and an IllegalStateException is thrown
 */
fun waitFor(timeout: Int, condition: () -> Boolean, name: String = "", errorAction: (() -> Any)? = null) {
    Logger.debug("waitUntil$name")
    val startTime = System.currentTimeMillis()
    while (!condition()) {
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime > timeout) {
            if(errorAction != null) {
                errorAction()
            }
            throw IllegalStateException("Timeout waiting for $name (after waiting $timeout ms)")
        }
        Thread.sleep(50)
    }
    Logger.debug("waitUntil$name finished")
}

