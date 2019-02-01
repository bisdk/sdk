enum class Command(val code: Int) {
    EMPTY(0),
    ERROR(1),
    GET_MAC(2),
    SET_VALUE(3),
    LOGIN(16),
    GET_NAME(38);

    companion object {
        fun valueOf(code: Int) = values().firstOrNull { it.code == code }
    }
}

data class Package(
        val command: Command,
        val tag: Int = 0,
        val token: Int = 0,
        val payload: Payload = Payload.empty()
) {
    fun toByteArray(): ByteArray {
        return getLength().toShort().toByteArray()
                .plus(tag.toByte().toByteArray())
                .plus(token.toByteArray())
                .plus(command.code.toByte().toByteArray()) // TODO if this is a response: set 8-th bit (| 128)
                .plus(payload.toByteArray())
                .plus(PackageChecksum(this).calculate().toByte().toByteArray())
    }

    fun getLength(): Int {
        return getFrameLength() + payload.toByteArray().size
    }

    private fun getFrameLength(): Int {
        return Lengths.FRAME_SIZE / Lengths.BYTE_LENGTH
    }

    fun toHexString(): String {
        return toByteArray().toHexString()
    }

    override fun toString(): String {
        return "command: $command, tag: $tag, token: $token, payload: $payload"
    }

    companion object {
        fun empty() = Package(Command.EMPTY)

        fun from(ba: ByteArray): Package {
            if(ba.size < Lengths.LENGTH_BYTES +  Lengths.TAG_BYTES + Lengths.TOKEN_BYTES + Lengths.COMMAND_BYTES) {
                return empty()
            }
            var idx = 0
            val length = (ba.copyOfRange(idx, idx + Lengths.LENGTH_BYTES).toHexString()).toInt(16)
            idx += Lengths.LENGTH_BYTES
            val tag = (ba.copyOfRange(idx, idx + Lengths.TAG_BYTES).toHexString()).toInt(16)
            idx += Lengths.TAG_BYTES
            val token = (ba.copyOfRange(idx, idx + Lengths.TOKEN_BYTES).toHexString()).toInt(16)
            idx += Lengths.TOKEN_BYTES
            val commandInt = (ba.copyOfRange(idx, idx + Lengths.COMMAND_BYTES).toHexString()).toInt(16)
            idx += Lengths.COMMAND_BYTES
            val command = Command.valueOf(commandInt)!!
            return Package(command = command, tag= tag, token = token, payload = Payload(ba.copyOfRange(idx, ba.size - 1)))
        }
    }

}

data class Payload(
        private val content: ByteArray
) {

    fun toByteArray(): ByteArray {
        return content
    }

    override fun toString(): String {
        return toByteArray().joinToString(separator = "") { encodeInt(it.toInt()) }
    }

    companion object {
        fun login(username: String, password: String) = Payload(username.length.toByte().toByteArray().plus(username.toByteArray()) + password.toByteArray())
        fun empty() = Payload(ByteArray(0))
    }
}

