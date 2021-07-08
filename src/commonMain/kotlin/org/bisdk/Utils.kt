package org.bisdk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Waits for the given condition to become true. Checks every 50ms if the condition is true.
 * If senderTimeout has exceeded the errorAction is executed (if exists) and an IllegalStateException is thrown
 */
suspend fun waitFor(timeout: Int, condition: () -> Boolean, name: String = "", errorAction: (() -> Any)? = null) =
    withContext(
        Dispatchers.Default
    ) {
        var waited = 0
        while (!condition()) {
            if (waited > timeout) {
                if (errorAction != null) {
                    errorAction()
                }
                throw IllegalStateException("Timeout waiting for $name (after waiting $timeout ms)")
            }
            waited += 50
            delay(50)
        }
    }
