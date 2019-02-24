package org.bisdk.sdk

import java.nio.ByteBuffer

/**
 * Converts the int to a 4-byte ByteArray
 */
fun Int.toByteArray() = ByteBuffer.allocate(4).putInt(this).array()!!

/**
 * Converts the int to a 2-byte ByteArray
 */
fun Short.toByteArray() = ByteBuffer.allocate(2).putShort(this).array()!!

/**
 * Converts the byte to a 1-byte ByteArray (just convenience method)
 */
fun Byte.toByteArray() = ByteArray(1).apply { set(0, this@toByteArray) }

/**
 * Converts the byte to a 2 digit hex value string
 */
fun Byte.toHexString() = toUByte().toString(16).padStart(2, '0').toUpperCase()

/**
 * Converts a byte as sent from the gateway to a normal hex string to be read easily (0x30 => "0")
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun Byte.toHexStringFromGW() = toString(16).toByte(16).toChar().toString().toUpperCase()

/**
 * Converts the byte array to a 2 digit per byte hex value string (easy to read)
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
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun String.toGWByteArray() = toByteArray()

/**
 * Converts a string as used internal to a byte array that can be used internally with one byte from 2 digits in string.
 *
 * The GW sends data by converting each digit to a byte and these bytes to a combined string (e.g. 0 => 30)
 */
fun String.toHexByteArray() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun String.toHexInt() = ByteBuffer.wrap(toHexByteArray()).int
