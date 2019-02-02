enum class PayloadType {
    MCP, JMCP
}

data class Payload(
        private val content: ByteArray,
        private val type : PayloadType = PayloadType.MCP
) {

    fun toByteArray(): ByteArray {
        return content
    }

    fun getContentAsString() = content.toHexStringFromGW()

    override fun toString(): String {
        return if(type == PayloadType.MCP) content.toHexString() else content.toHexStringFromGW()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Payload

        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        return content.contentHashCode()
    }

    companion object {
        fun login(username: String, password: String) = Payload(username.length.toByte().toByteArray().plus(username.toGWByteArray()) + password.toGWByteArray())
        fun jmcp(content: String) = Payload(content.toGWByteArray(), PayloadType.JMCP)
        fun getTransition(value: Byte) = Payload(value.toByteArray())  // HM_GET_TRANSITION
        fun getGroupsForUser() = jmcp("{\"CMD\":\"GET_GROUPS\", \"FORUSER\":1}")  // GET_GROUPS
        fun setState(portId: Int, state: Int = 0xFF) = Payload(portId.toByte().toByteArray().plus(state.toByte()))  // SET_STATE (example "00FF")
        fun empty() = Payload(ByteArray(0))
    }
}
