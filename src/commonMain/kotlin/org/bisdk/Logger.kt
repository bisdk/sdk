package org.bisdk

import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/**
 * Abstraction of a basic logger to be used by external applications.
 *
 * Set your own LoggerAdapter and include BiSdk in your own logging framework.
 */
class Logger {

    companion object {
        private var internalAdapter: LoggerAdapter = PrintlnAdapter()
        fun setLoggerAdapter(newLoggerAdapter: LoggerAdapter) {
            internalAdapter = newLoggerAdapter
        }

        fun debug(message: String) {
            internalAdapter.debug(message)
        }

        fun info(message: String) {
            internalAdapter.info(message)
        }

        fun warn(message: String) {
            internalAdapter.warn(message)
        }
    }
}

@OptIn(ExperimentalTime::class)
class PrintlnAdapter : LoggerAdapter {
    override fun debug(message: String) {
        println(Clock.System.now().toString() + " DEBUG: " + message)
    }

    override fun info(message: String) {
        println(Clock.System.now().toString() + " INFO: " + message)
    }

    override fun warn(message: String) {
        println(Clock.System.now().toString() + " WARN: " + message)
    }
}

class NoopAdapter : LoggerAdapter {
    override fun debug(message: String) {
    }

    override fun info(message: String) {
    }

    override fun warn(message: String) {
    }
}

interface LoggerAdapter {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
}
