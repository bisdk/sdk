class Checksum(val pack: Package) {

    fun calculate(): Int {
        var value = pack.getLength()
        value += pack.tag
        value += (pack.token.toInt() and 255)
        value += (pack.token.toInt() shr 8 and 255)
        value += (pack.token.toInt() shr 16 and 255)
        value += (pack.token.toInt() shr 24 and 255)
        value += pack.command.code
        pack.payload.toByteArray().forEach {
            value += it.toByte()
        }
        value = value and 255
        return value
    }

}
