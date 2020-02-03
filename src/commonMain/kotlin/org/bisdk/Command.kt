package org.bisdk

/**
 * The protocol between the client and the bisecure gateway uses a 1-byte command to signal whats to be done.
 *
 * In the result from the GW the command has the 7-th bit set to signal a response
 */
class Command(val code: Int, val name: String = "UNKNOWN", val authenticationRequired: Boolean = true) {

    companion object {
        fun valueOf(code: Int) = Command.values.firstOrNull { it.code == code } ?: Command(
            code
        )

        val EMPTY = Command(-1, "EMPTY", false)
        val PING = Command(0, "PING", false)
        val ERROR = Command(1, "ERROR", false)
        val GET_MAC = Command(2, "GET_MAC", false)
        val SET_VALUE = Command(3, "SET_VALUE")
        val JMCP = Command(6, "JMCP")
        val LOGIN = Command(16, "LOGIN", false)
        val LOGOUT = Command(17, "LOGOUT")
        val GET_NAME = Command(38, "GET_NAME", false)
        val SET_STATE = Command(51, "SET_STATE")
        val HM_GET_TRANSITION = Command(112, "HM_GET_TRANSITION")

        val values = arrayListOf(
            Command.EMPTY,
            Command.PING,
            Command.ERROR,
            Command.GET_MAC,
            Command.SET_VALUE,
            Command.JMCP,
            Command.LOGIN,
            Command.LOGOUT,
            Command.GET_NAME,
            Command.SET_STATE,
            Command.HM_GET_TRANSITION
        )
    }

    override fun toString(): String {
        return "Command(code=$code (${code.toString(16)}), name='$name')"
    }
}
