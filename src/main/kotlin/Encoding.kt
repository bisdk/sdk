fun encodeInt(value: Int) = (value).toString(16).padStart(2, '0').toUpperCase()
fun encodeString(value: String) = (value).map { encodeInt(it.toInt()) }.joinToString(separator = "")
