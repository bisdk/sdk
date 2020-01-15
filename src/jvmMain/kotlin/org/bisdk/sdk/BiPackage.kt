package org.bisdk.sdk

import org.bisdk.*

/**
 * A BiPackage is sent over the socket to the gateway. It contains the command and the payload.
 */
data class BiPackage(
    val command: Command,
    val tag: Int = 0,
    val token: String = "00000000",
    val payload: Payload = Payload.empty(),
    val isResponse: Boolean = false
) {
    init {
        assert(token.length == 8)
    }
    fun toByteArray(): ByteArray {
        return getLength().toByteArray(2)
            .plus(tag.toByte().toByteArray())
                .plus(token.toHexByteArray())
                .plus(command.code.toByte().toByteArray())
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

    fun getBiError(): BiError? = BiError.from(payload.toByteArray()[0].toInt())

    override fun toString(): String {
        if(command == Command.ERROR) {
            return "command: $command, tag: $tag, token: $token, error: ${getBiError()}, isResponse=$isResponse"
        }
        if(command == Command.HM_GET_TRANSITION && isResponse) {
            return "command: $command, tag: $tag, token: $token, payload: ${Transition.from(payload.toByteArray())}, isResponse=$isResponse"
        }
        return "command: $command, tag: $tag, token: $token, payload: $payload, isResponse=$isResponse"
    }

    companion object {
        fun empty() = BiPackage(Command.EMPTY)
        fun login(username: String, password: String) =
            BiPackage(command = Command.LOGIN, payload = Payload.login(username, password))

        fun jmcp(content: String) = BiPackage(command = Command.JMCP, payload = Payload.jmcp(content))

        fun from(ba: ByteArray): BiPackage {
            if(ba.size < Lengths.LENGTH_BYTES +  Lengths.TAG_BYTES + Lengths.TOKEN_BYTES + Lengths.COMMAND_BYTES) {
                return empty()
            }
            var idx = 0
            val length = (ba.copyOfRange(idx, idx + Lengths.LENGTH_BYTES).toHexString()).toInt(16)
            idx += Lengths.LENGTH_BYTES
            val tag = (ba.copyOfRange(idx, idx + Lengths.TAG_BYTES).toHexString()).toInt(16)
            idx += Lengths.TAG_BYTES
            val token = ba.copyOfRange(idx, idx + Lengths.TOKEN_BYTES).toHexString()
            idx += Lengths.TOKEN_BYTES
            var commandInt = (ba.copyOfRange(idx, idx + Lengths.COMMAND_BYTES).toHexString()).toInt(16)
            var isResponse = false
            if(commandInt.testBit(7)) {
               commandInt  = commandInt xor (1 shl 7)
                isResponse = true
            }
            idx += Lengths.COMMAND_BYTES
            val command = Command.valueOf(commandInt)
            val payloadLength = length - Lengths.LENGTH_BYTES - Lengths.TAG_BYTES - Lengths.TOKEN_BYTES - Lengths.COMMAND_BYTES
            if(ba.size < idx + payloadLength) {
                return empty()
            }
            val payloadBytes = ba.copyOfRange(idx, idx + payloadLength - 1)
            return BiPackage(command, tag, token, Payload(payloadBytes), isResponse)
        }

        fun fromHexString(hex: String): BiPackage {
            return from(hex.toHexByteArray())
        }
    }

}

