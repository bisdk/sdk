import java.nio.ByteBuffer

fun encodeInt(value: Int, length: Int = 2) = (value).toString(16).padStart(length, '0').toUpperCase()
fun encodeByte(value: UByte, length: Int = 2) = (value).toString(16).padStart(length, '0').toUpperCase()

fun Int.toByteArray() = ByteBuffer.allocate(4).putInt(this).array()!!
fun Short.toByteArray() = ByteBuffer.allocate(2).putShort(this).array()!!
fun Byte.toByteArray() = ByteArray(1).apply { set(0, this@toByteArray) }

fun Byte.toHexString() = toUByte().toString(16).padStart(2, '0').toUpperCase()
fun Byte.toHexStringFromTC() = toString(16).toByte(16).toChar().toString().toUpperCase()
fun ByteArray.toHexString() = joinToString(separator = "") { it.toHexString() }
fun ByteArray.toHexStringFromTC() = joinToString(separator = "") { it.toHexStringFromTC() }
fun ByteArray.decodeTC() = toHexStringFromTC().toHexByteArray()
fun ByteArray.encodeTC() = toHexString().toTCByteArray()
fun String.toTCByteArray() = toByteArray()
fun String.toHexByteArray() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
