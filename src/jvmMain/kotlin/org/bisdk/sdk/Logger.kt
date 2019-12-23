package org.bisdk.sdk

class Logger {

    companion object {
        var isDebugEnabled: Boolean = false
        fun log(message: String) {
            if(isDebugEnabled) {
                println(message)
            }
        }
    }
}
