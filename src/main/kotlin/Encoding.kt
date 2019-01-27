fun encodeInt(value: Int, length: Int = 2) = (value).toString(16).padStart(length, '0').toUpperCase()
fun encodeInt(value: UInt, length: Int = 2) = (value).toString(16).padStart(length, '0').toUpperCase()
fun encodeString(value: String) = (value).map { encodeInt(it.toInt()) }.joinToString(separator = "")
