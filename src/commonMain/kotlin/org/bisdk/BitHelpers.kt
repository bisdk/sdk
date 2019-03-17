package org.bisdk

fun Int.testBit(bit: Int) = this.and(1.shl(bit)) != 0
