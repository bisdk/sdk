package org.bisdk.sdk

class TransportContainerChecksum(val tp: TransportContainer) {

    fun calculate(): Byte {
        var value = 0
        val str = getChecksumSource()
        str.forEach {
            value += it.toByte()
        }
        value = value and 255
        return value.toByte()
    }

    fun getChecksumSource() = tp.sender + tp.receiver + tp.pack.toHexString()

}
