class PackageChecksum(val pack: Package) {

    fun calculate(): Int {
        var value = pack.getLength()
        value += pack.tag
        value += (pack.token and 255)
        value += (pack.token shr 8 and 255)
        value += (pack.token shr 16 and 255)
        value += (pack.token shr 24 and 255)
        value += pack.command.code
        pack.payload.toByteArray().forEach {
            value += it
        }
        value = value and 255
        return value
    }

}
