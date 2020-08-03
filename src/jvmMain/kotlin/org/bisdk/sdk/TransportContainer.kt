package org.bisdk.sdk

import org.bisdk.*

/**
 * All messages (Packages) are encapsulated inside a transport container. It defines the sender, receiver and a checksum.
 */
class TransportContainer(
    val sender: String,
    val receiver: String,
    val pack: BiPackage,
    val checksum: Byte
) {
    val calculatedChecksum = TransportContainerChecksum().calculate(this)

    fun hasCorrectChecksum() = checksum == calculatedChecksum

    fun toByteArray() =
        sender.toHexByteArray().plus(receiver.toHexByteArray()).plus(pack.toByteArray()).plus(checksum.toByteArray())

    fun toHexString() = sender + receiver + pack.toHexString() + checksum.toHexString()

    fun size() = toByteArray().size / 2

    override fun toString() = "sender: $sender, receiver: $receiver, pack: $pack, checksum: ${checksum.toHexString()}"

    companion object {
        fun from(ba: ByteArray): TransportContainer {
            if (ba.size < Lengths.ADDRESS_BYTES * 2) {
                throw IllegalArgumentException("Wrong size: " + ba.toHexString())
            }
            val sender = ba.copyOfRange(0, Lengths.ADDRESS_BYTES).toHexString()
            val receiver = ba.copyOfRange(Lengths.ADDRESS_BYTES, Lengths.ADDRESS_BYTES * 2).toHexString()
            val biPackage = ba.copyOfRange(Lengths.ADDRESS_BYTES * 2, ba.size - Lengths.CHECKSUM_BYTES)
//            Logger.debug("sender: $sender, receiver: $receiver, biPackage: ${biPackage.toHexString()}")
            val pack = BiPackage.from(biPackage)
            if(pack.command == Command.EMPTY) {
                // EMPTY command => we do not even check last checksum
                return TransportContainer(sender, receiver, pack, 0)
            }
            val checksum = ba.copyOfRange(Lengths.ADDRESS_BYTES * 2 + pack.getLength(), Lengths.ADDRESS_BYTES * 2 + pack.getLength() + Lengths.CHECKSUM_BYTES).toHexString().toInt(16).toByte()
            return TransportContainer(sender, receiver, pack, checksum)
        }

        fun fromHexString(hex: String): TransportContainer {
            val sender = hex.substring(0, Lengths.ADDRESS_SIZE)
            val receiver = hex.substring(Lengths.ADDRESS_SIZE, Lengths.ADDRESS_SIZE * 2)
            val pack =
                BiPackage.fromHexString(hex.substring(Lengths.ADDRESS_SIZE * 2, hex.length - Lengths.CHECKSUM_SIZE))
            val checksum = hex.substring(hex.length - Lengths.CHECKSUM_SIZE, hex.length).toInt(16).toByte()
            return TransportContainer(sender, receiver, pack, checksum)
        }

        fun create(sender: String, receiver: String, pack: BiPackage): TransportContainer {
            return TransportContainer(
                sender,
                receiver,
                pack,
                TransportContainerChecksum().calculate(sender, receiver, pack)
            )
        }
    }
}
