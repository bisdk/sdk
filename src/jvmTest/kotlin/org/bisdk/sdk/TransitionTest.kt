package org.bisdk.sdk

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class TransitionTest {

    @Test
    fun testClosedDoor() {
        val ba = "00000000010802020000000000000000".toHexByteArray()
        val tr = Transition.from(ba)
        println(tr)
        assertThat(tr.hcp.positionClose).isTrue()
        assertThat(tr.hcp.positionOpen).isFalse()
    }

    @Test
    fun testOpenDoor() {
        val ba = "C8C80000010801020000000000000000".toHexByteArray()
        val tr = Transition.from(ba)
        println(tr)
        assertThat(tr.hcp.positionClose).isFalse()
        assertThat(tr.hcp.positionOpen).isTrue()
    }
}
