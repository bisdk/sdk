package org.bisdk.sdk

class TransportContainerChecksum() {

    fun calculate(tp: TransportContainer): Byte {
        val sender = tp.sender
        val receiver = tp.receiver
        val pack = tp.pack
        return calculate(sender, receiver, pack)
    }

    fun calculate(sender: String, receiver: String, pack: BiPackage): Byte {
        val str = sender + receiver + pack.toHexString()
        var value = 0
        str.forEach {
            value += it.toByte()
        }
        value = value and 255
        return value.toByte()
    }

}
