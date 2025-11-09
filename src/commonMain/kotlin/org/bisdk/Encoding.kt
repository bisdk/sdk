package org.bisdk

/**
 * Converts the int to a ByteArray of given size (big-endian)
 */
fun Int.toByteArray(size: Int) = ByteArray(size) { this.ushr((size - 1 - it) * 8).toByte() }

/**
 * Converts the int to a ByteArray of given size (big-endian)
 */
fun Int.toByteArray() = ByteArray(4) { this.ushr((3 - it) * 8).toByte() }

/**
 * Converts the byte to a 1-byte ByteArray (just convenience method)
 */
fun Byte.toByteArray() = ByteArray(1).apply { this[0] = this@toByteArray }

/**
 * Converts the byte to a 2 digit hex value string
 */
fun Byte.toHexString() = toUByte().toString(16).padStart(2, '0').uppercase()

/**
 * Converts a byte as sent from the gateway to a normal hex string to be read easily (0x30 => "0")
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun Byte.toHexStringFromGW() = toString(16).toByte(16).toChar().toString().uppercase()

/**
 * Converts a byte array to a 2 digit per byte hex value string (easy to read)
 */
fun ByteArray.toHexString() = joinToString(separator = "") { it.toHexString() }

/**
 * Converts a byte array as sent from the gateway to a normal hex string to be read easily (0x30 => "0")
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun ByteArray.toHexStringFromGW() = joinToString(separator = "") { it.toHexStringFromGW() }

/**
 * Converts a byte array as sent from the gateway to a normal byte array to be read easily (0x30, 0x30 => 0x00).
 * The resulting byte array has half the size.
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun ByteArray.decodeFromGW() = toHexStringFromGW().toHexByteArray()

/**
 * Converts a byte array as used internal to a byte array that can be sent to the gateway (0x00 => 0x30, 0x30).
 * The resulting byte array has double the size.
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun ByteArray.encodeToGW() = toHexString().toGWByteArray()

/**
 * Converts a string as used internal to a byte array that can be sent to the gateway ("00" => 0x30, 0x30).
 * Uses UTF-8 encoding which matches default gateway expectation.
 */
fun String.toGWByteArray() = encodeToByteArray()

/**
 * Converts a string as used internal to a byte array that can be used internally with one byte from 2 digits in string.
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun String.toHexByteArray() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun String.toHexInt(): Int {
    val bytes = toHexByteArray()
    require(bytes.size >= 4) { "Need at least 4 bytes to convert to Int" }
    return (bytes[0].toInt() and 0xFF shl 24) or
        (bytes[1].toInt() and 0xFF shl 16) or
        (bytes[2].toInt() and 0xFF shl 8) or
        (bytes[3].toInt() and 0xFF)
}