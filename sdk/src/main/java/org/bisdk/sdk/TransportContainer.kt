package org.bisdk.sdk

/**
 * All messages (Packages) are encapsulated inside a transport container. It defines the sender, receiver and a checksum.
 */
class TransportContainer(
    val sender: String,
    val receiver: String,
    val pack: Package
) {
    private val checksum = TransportContainerChecksum(this).calculate()

    fun toByteArray() =
        sender.toHexByteArray().plus(receiver.toHexByteArray()).plus(pack.toByteArray()).plus(checksum.toByteArray())

    fun toHexString() = sender + receiver + pack.toHexString() + checksum.toHexString()

    override fun toString() = "sender: $sender, receiver: $receiver, pack: $pack, checksum: ${checksum.toHexString()}"

    companion object {
        fun from(ba: ByteArray): TransportContainer {
            if (ba.size < Lengths.ADDRESS_BYTES * 2) {
                throw IllegalArgumentException("Wrong size: " + ba.toHexString())
            }
            val sender = ba.copyOfRange(0, Lengths.ADDRESS_BYTES).toHexString()
            val receiver = ba.copyOfRange(Lengths.ADDRESS_BYTES, Lengths.ADDRESS_BYTES * 2).toHexString()
            val pack = Package.from(ba.copyOfRange(Lengths.ADDRESS_BYTES * 2, ba.size))
            return TransportContainer(sender, receiver, pack)
        }

        fun fromHexString(hex: String): TransportContainer {
            val sender = hex.substring(0, Lengths.ADDRESS_SIZE)
            val receiver = hex.substring(Lengths.ADDRESS_SIZE, Lengths.ADDRESS_SIZE * 2)
            val pack = Package.fromHexString(hex.substring(Lengths.ADDRESS_SIZE * 2, hex.length - 1))
            return TransportContainer(sender, receiver, pack)
        }
    }
}
