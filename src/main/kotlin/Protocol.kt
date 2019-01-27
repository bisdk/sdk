import java.nio.ByteBuffer

enum class Command(val code: Int) {
    LOGIN(16),
    GET_NAME(38)
}

class Length {

    companion object {
        val BYTE_LENGTH = 2
        val ADDRESS_SIZE = 6 * BYTE_LENGTH
        val LENGTH_SIZE = 2 * BYTE_LENGTH
        val TAG_SIZE = 1 * BYTE_LENGTH
        val TOKEN_SIZE = 4 * BYTE_LENGTH
        val COMMAND_SIZE = 1 * BYTE_LENGTH
        val CHECKSUM_SIZE = 1 * BYTE_LENGTH
        val FRAME_SIZE = LENGTH_SIZE + TAG_SIZE + TOKEN_SIZE + COMMAND_SIZE + CHECKSUM_SIZE
    }
}

data class Package(
        val command: Command,
        val tag: Int = 0,
        val token: Int = 0,
        val payload: Payload
) {
    fun toByteArray(): ByteArray {
        return getLength().toShort().toByteArray()
                .plus(tag.toByte().toByteArray())
                .plus(token.toByteArray())
                .plus(command.code.toByte().toByteArray()) // TODO if this is a response: set 8-th bit (| 128)
                .plus(payload.toByteArray())
                .plus(getChecksum().toByte().toByteArray())
    }

    fun getChecksum(): Int {
        var value = getLength()
        value += this.tag
        value += (this.token.toInt() and 255)
        value += (this.token.toInt() shr 8 and 255)
        value += (this.token.toInt() shr 16 and 255)
        value += (this.token.toInt() shr 24 and 255)
        value += this.command.code
        payload.toByteArray().forEach {
            value += it.toByte()
        }
        value = value and 255
        return value

    }

    fun getFrameLength(): Int {
        return Length.FRAME_SIZE / Length.BYTE_LENGTH
    }

    fun getLength(): Int {
        return getFrameLength() + payload.toByteArray().size
    }
}

class Payload(val content: ByteArray) {

    fun toByteArray(): ByteArray {
        return content
    }

    companion object {
        fun login(username: String, password: String) = Payload(username.length.toByte().toByteArray().plus(username.toByteArray())+ password.toByteArray())
    }
}

fun Int.toByteArray() = ByteBuffer.allocate(4).putInt(this).array()
fun Short.toByteArray() = ByteBuffer.allocate(2).putShort(this).array()
fun Byte.toByteArray() = ByteArray(1).apply { set(0, this@toByteArray) }
