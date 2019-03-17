package org.bisdk.sdk

import org.assertj.core.api.Assertions.assertThat
import org.bisdk.Command
import org.bisdk.Payload
import org.bisdk.toHexByteArray
import org.bisdk.toHexString
import org.junit.Test

internal class TransportContainerKtTest {

    @Test
    fun toByteArrayLoginPackage() {
        val expected = "0000000000005410EC03615000190000000000100674686F6D61736161616262626363632DF0"
        val tc = TransportContainer(
            "000000000000",
            "5410EC036150",
            BiPackage(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc"))
        )
        assertThat(tc.toByteArray().toHexString()).isEqualTo(expected)
    }

    @Test
    fun fromByteArrayEmpty() {
        val tc = TransportContainer.from("5410EC036150000000000006001800".toHexByteArray())
        assertThat(tc).isNotNull
        assertThat(tc.sender).isEqualTo("5410EC036150")
        assertThat(tc.receiver).isEqualTo("000000000006")
        val pack = tc.pack
        assertThat(pack.tag).isEqualTo(0)
        assertThat(pack.token).isEqualTo("00000000")
        assertThat(pack.command).isEqualTo(Command.EMPTY)
    }

    @Test
    fun fromByteArrayLoginResponse() {
        val tc = TransportContainer.from("5410EC036150000000000006001800".toHexByteArray())
        assertThat(tc).isNotNull
        assertThat(tc.sender).isEqualTo("5410EC036150")
        assertThat(tc.receiver).isEqualTo("000000000006")
        val pack = tc.pack
        assertThat(pack.tag).isEqualTo(0)
        assertThat(pack.token).isEqualTo("00000000")
        assertThat(pack.command).isEqualTo(Command.EMPTY)
    }
}
