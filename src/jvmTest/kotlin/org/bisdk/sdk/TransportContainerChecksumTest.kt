package org.bisdk.sdk

import org.assertj.core.api.Assertions
import org.bisdk.Command
import org.bisdk.Payload
import org.junit.Test

internal class TransportContainerChecksumTest {

    @Test
    fun calculate() {
        val tp = TransportContainer.create("000000000000", "5410EC036150", BiPackage.fromCommandAndPayload(Command.GET_NAME, Payload.empty()))
        val cs = TransportContainerChecksum().calculate(tp)
        Assertions.assertThat(cs).isEqualTo(0x4A)
    }

    @Test
    fun calculateDirectly() {
        val cs = TransportContainerChecksum().calculate("000000000000", "5410EC036150", BiPackage.fromCommandAndPayload(Command.GET_NAME, Payload.empty()))
        Assertions.assertThat(cs).isEqualTo(0x4A)
    }
}
