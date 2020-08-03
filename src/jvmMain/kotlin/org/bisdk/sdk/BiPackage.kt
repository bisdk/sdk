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
    val isResponse: Boolean = false,
    val checksum: Byte = 0
) {

    val calculatedChecksum = PackageChecksum(this).calculate().toByte()

    init {
        assert(token.length == 8)
    }

    fun hasCorrectChecksum() = checksum == calculatedChecksum

    fun toByteArray(): ByteArray {
        return getLength().toByteArray(2)
            .plus(tag.toByte().toByteArray())
            .plus(token.toHexByteArray())
            .plus(getCommandCode().toByteArray())
            .plus(payload.toByteArray())
            .plus(calculatedChecksum.toByteArray())
    }

    fun getCommandCode(): Byte {
        val code = command.code
        if(isResponse) {
            val flipped = code or (1 shl 7)
            return flipped.toByte()
        }
        return code.toByte()
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
        if (command == Command.ERROR) {
            return "command: $command, tag: $tag, token: $token, error: ${getBiError()}, isResponse=$isResponse"
        }
        if (command == Command.HM_GET_TRANSITION && isResponse) {
            return "command: $command, tag: $tag, token: $token, payload: ${Transition.from(payload.toByteArray())}, isResponse=$isResponse"
        }
        return "command: $command, tag: $tag, token: $token, payload: $payload, isResponse=$isResponse, checksum=$checksum"
    }

    companion object {
        fun empty() = BiPackage(Command.EMPTY)
        fun login(username: String, password: String) =
            fromCommandAndPayload(command = Command.LOGIN, payload = Payload.login(username, password))

        fun jmcp(content: String) = fromCommandAndPayload(command = Command.JMCP, payload = Payload.jmcp(content))

        fun fromCommandAndPayload(command: Command, payload: Payload): BiPackage {
            val packWithoutChecksum = BiPackage(command = command, payload = payload)
            val checksum = PackageChecksum(packWithoutChecksum).calculate().toByte()
            return BiPackage(command = command, payload = payload, checksum = checksum)
        }

        fun from(ba: ByteArray): BiPackage {
            if (ba.size < Lengths.LENGTH_BYTES + Lengths.TAG_BYTES + Lengths.TOKEN_BYTES + Lengths.COMMAND_BYTES) {
                return empty()
            }
            var idx = 0
            val lengthHexString = ba.copyOfRange(idx, idx + Lengths.LENGTH_BYTES).toHexString()
            val length = lengthHexString.toInt(16)
            idx += Lengths.LENGTH_BYTES
            val tagHexString = ba.copyOfRange(idx, idx + Lengths.TAG_BYTES).toHexString()
            val tag = tagHexString.toInt(16)
            idx += Lengths.TAG_BYTES
            val token = ba.copyOfRange(idx, idx + Lengths.TOKEN_BYTES).toHexString()
            idx += Lengths.TOKEN_BYTES
            val commandHexString = ba.copyOfRange(idx, idx + Lengths.COMMAND_BYTES).toHexString()
            var commandInt = commandHexString.toInt(16)
            var isResponse = false
            if (commandInt.testBit(7)) {
                commandInt = commandInt xor (1 shl 7)
                isResponse = true
            }
            idx += Lengths.COMMAND_BYTES
            val command = Command.valueOf(commandInt)
//            Logger.debug("length: $lengthHexString, tag: $tagHexString, token: $token, command: $commandHexString ($command)")
            val payloadLength =
                length - Lengths.LENGTH_BYTES - Lengths.TAG_BYTES - Lengths.TOKEN_BYTES - Lengths.COMMAND_BYTES - Lengths.CHECKSUM_BYTES
            if (payloadLength < 0 || ba.size < length) {
                return empty()
            }
//            Logger.debug("ba.size: ${ba.size}, idx: $idx, Payload length: $payloadLength")
            val payloadBytes = ba.copyOfRange(idx, idx + payloadLength)
            val checksum = ba[idx + payloadLength]
//            Logger.debug("Payload: ${payloadBytes.toHexString()} Checksum: ${checksum.toHexString()}")
            val biPackage = BiPackage(command, tag, token, Payload(payloadBytes), isResponse, checksum)
//            Logger.debug("Parsed BiPackage: ${biPackage.toHexString()}")
            if (!biPackage.hasCorrectChecksum()) {
//                Logger.debug("Incorrect checksum in package: parsed: $checksum, calculated: ${biPackage.calculatedChecksum}")
                return empty()
            }
            return biPackage
        }

        fun fromHexString(hex: String): BiPackage {
            return from(hex.toHexByteArray())
        }
    }

}

