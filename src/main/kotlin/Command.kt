/**
 * The protocol between the client and the bisecure gateway uses a 1-byte command to signal whats to be done.
 *
 * In the result from the GW the command has the 7-th bit set to signal a response
 */
class Command(val code: Int, val name: String = "UNKNOWN") {

    companion object {
        fun valueOf(code: Int) = values.firstOrNull { it.code == code } ?: Command(code)
        val EMPTY = Command(0, "EMPTY")
        val ERROR = Command(1, "ERROR")
        val GET_MAC = Command(2, "GET_MAC")
        val SET_VALUE = Command(3, "SET_VALUE")
        val JMCP = Command(6, "JMCP")
        val LOGIN = Command(16, "LOGIN")
        val LOGOUT = Command(17, "LOGOUT")
        val GET_NAME = Command(38, "GET_NAME")
        val SET_STATE = Command(51, "SET_STATE")
        val HM_GET_TRANSITION = Command(112, "HM_GET_TRANSITION")

        val values = arrayListOf(EMPTY, ERROR, GET_MAC, SET_VALUE, JMCP, LOGIN, LOGOUT, GET_NAME, SET_STATE, HM_GET_TRANSITION)
    }

    override fun toString(): String {
        return "Command(code=$code (${code.toString(16)}), name='$name')"
    }
}
