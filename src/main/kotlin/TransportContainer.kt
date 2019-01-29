class TransportContainer(
        val sender: String,
        val receiver: String,
        val pack: Package
) {
    fun toByteArray() = (sender + receiver + pack.toString() + encodeByte(TransportContainerChecksum(this).calculate().toUByte(), 1)).toByteArray()

    override fun toString() = sender + receiver + pack.toString() + encodeByte(TransportContainerChecksum(this).calculate().toUByte(), 1)
}

fun String.parseHexToByteArray(): ByteArray {
    val byteAmount = length / 2
    val byteArray = ByteArray(byteAmount)
    (0 until byteAmount).forEach { byteArray[it] = substring(it*2, (it+1)*2).toUByte(16).toByte() }
    return byteArray
}
