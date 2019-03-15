package org.bisdk.sdk

import org.bisdk.toHexInt

class PackageChecksum(val pack: BiPackage) {

    fun calculate(): Int {
        var value = pack.getLength()
        value += pack.tag
        value += (pack.token.toHexInt() and 255)
        value += (pack.token.toHexInt() shr 8 and 255)
        value += (pack.token.toHexInt() shr 16 and 255)
        value += (pack.token.toHexInt() shr 24 and 255)
        value += pack.command.code
        pack.payload.toByteArray().forEach {
            value += it
        }
        value = value and 255
        return value
    }

}
