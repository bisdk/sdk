package org.bisdk.sdk

import org.assertj.core.api.Assertions
import org.junit.Test

internal class TransportContainerChecksumTest {

    @Test
    fun calculate() {
        val tp = TransportContainer("000000000000", "5410EC036150", Package(org.bisdk.sdk.Command.GET_NAME))
        val cs = TransportContainerChecksum(tp).calculate()
        Assertions.assertThat(cs).isEqualTo(0x4A)
    }

    @Test
    fun testChecksumSource() {
        val tp = TransportContainer("000000000000", "5410EC036150", Package(org.bisdk.sdk.Command.GET_NAME))
        val cs = TransportContainerChecksum(tp).getChecksumSource()
        Assertions.assertThat(cs).isEqualTo("0000000000005410EC03615000090000000000262F")
    }
}
