package org.bisdk.sdk

class Logger {

    companion object {
        var isDebugEnabled: Boolean = false
        var isInfoEnabled: Boolean = true
        var isWarnEnabled: Boolean = true
        fun setDebugLevel() {
            isDebugEnabled = true
            isInfoEnabled = true
            isWarnEnabled = true
        }
        fun setInfoLevel() {
            isDebugEnabled = false
            isInfoEnabled = true
            isWarnEnabled = true
        }
        fun setWarnLevel() {
            isDebugEnabled = false
            isInfoEnabled = false
            isWarnEnabled = true
        }
        fun setNoopLevel() {
            isDebugEnabled = false
            isInfoEnabled = false
            isWarnEnabled = false
        }
        fun debug(message: String) {
            if(isDebugEnabled) {
                println(message)
            }
        }
        fun info(message: String) {
            if(isInfoEnabled) {
                println(message)
            }
        }
        fun warn(message: String) {
            if(isWarnEnabled) {
                println(message)
            }
        }
    }
}
