package org.bisdk.sdk

/**
 * The protocol between the client and the bisecure gateway uses a 1-byte command to signal whats to be done.
 *
 * In the result from the GW the command has the 7-th bit set to signal a response
 */
class Command(val code: Int, val name: String = "UNKNOWN") {

    companion object {
        fun valueOf(code: Int) = org.bisdk.sdk.Command.Companion.values.firstOrNull { it.code == code } ?: org.bisdk.sdk.Command(
            code
        )
        val EMPTY = org.bisdk.sdk.Command(-1, "EMPTY")
        val PING = org.bisdk.sdk.Command(0, "PING")
        val ERROR = org.bisdk.sdk.Command(1, "ERROR")
        val GET_MAC = org.bisdk.sdk.Command(2, "GET_MAC")
        val SET_VALUE = org.bisdk.sdk.Command(3, "SET_VALUE")
        val JMCP = org.bisdk.sdk.Command(6, "JMCP")
        val LOGIN = org.bisdk.sdk.Command(16, "LOGIN")
        val LOGOUT = org.bisdk.sdk.Command(17, "LOGOUT")
        val GET_NAME = org.bisdk.sdk.Command(38, "GET_NAME")
        val SET_STATE = org.bisdk.sdk.Command(51, "SET_STATE")
        val HM_GET_TRANSITION = org.bisdk.sdk.Command(112, "HM_GET_TRANSITION")

        val values = arrayListOf(
            org.bisdk.sdk.Command.Companion.EMPTY,
            org.bisdk.sdk.Command.Companion.PING,
            org.bisdk.sdk.Command.Companion.ERROR,
            org.bisdk.sdk.Command.Companion.GET_MAC,
            org.bisdk.sdk.Command.Companion.SET_VALUE,
            org.bisdk.sdk.Command.Companion.JMCP,
            org.bisdk.sdk.Command.Companion.LOGIN,
            org.bisdk.sdk.Command.Companion.LOGOUT,
            org.bisdk.sdk.Command.Companion.GET_NAME,
            org.bisdk.sdk.Command.Companion.SET_STATE,
            org.bisdk.sdk.Command.Companion.HM_GET_TRANSITION
        )
    }

    override fun toString(): String {
        return "Command(code=$code (${code.toString(16)}), name='$name')"
    }
}
