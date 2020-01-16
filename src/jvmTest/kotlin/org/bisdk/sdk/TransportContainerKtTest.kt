package org.bisdk.sdk

import org.assertj.core.api.Assertions.assertThat
import org.bisdk.Command
import org.bisdk.Payload
import org.bisdk.toHexByteArray
import org.bisdk.toHexString
import org.junit.Test

internal class TransportContainerKtTest {

    @Test
    fun `check that byte array from tc login is correct`() {
        val expected = "0000000000005410EC03615000190000000000100674686F6D61736161616262626363632DF0"
        val tc = TransportContainer.create(
            "000000000000",
            "5410EC036150",
            BiPackage.login("thomas", "aaabbbccc")
        )
        assertThat(tc.toByteArray().toHexString()).isEqualTo(expected)
    }

    @Test
    fun `check that byte array from tc getName is correct`() {
        val expected = "0000000000005410EC03615000090000000000262F4A"
        val biPackage = BiPackage.fromCommandAndPayload(Command.GET_NAME, Payload.empty())
        val tc = TransportContainer.create(
            "000000000000",
            "5410EC036150",
            biPackage
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
    fun fromByteArrayErrorResponse() {
        val tc = TransportContainer.from("5410EC036150000000000006000A007F162664010C36EB".toHexByteArray())
        assertThat(tc).isNotNull
        assertThat(tc.sender).isEqualTo("5410EC036150")
        assertThat(tc.receiver).isEqualTo("000000000006")
        val pack = tc.pack
        assertThat(pack.tag).isEqualTo(0)
        assertThat(pack.token).isEqualTo("7F162664")
        assertThat(pack.command).isEqualTo(Command.ERROR)
        assertThat(pack.checksum).describedAs("Correct pack checksum").isEqualTo(pack.calculatedChecksum)
        assertThat(tc.checksum).describedAs("Correct checksum").isEqualTo(tc.calculatedChecksum)
    }

    @Test
    fun fromByteArrayPingResponse() {
        val tc = TransportContainer.from("5410EC03615000000000000600090200000000808B54".toHexByteArray())
        assertThat(tc).isNotNull
        assertThat(tc.sender).isEqualTo("5410EC036150")
        assertThat(tc.receiver).isEqualTo("000000000006")
        val pack = tc.pack
        assertThat(pack.tag).isEqualTo(2)
        assertThat(pack.token).isEqualTo("00000000")
        assertThat(pack.command).isEqualTo(Command.PING)
        assertThat(pack.checksum).describedAs("Correct pack checksum").isEqualTo(pack.calculatedChecksum)
        assertThat(tc.checksum).describedAs("Correct checksum").isEqualTo(tc.calculatedChecksum)
    }

    @Test
    fun fromByteArrayGetNameRequest() {
        val tc = TransportContainer.from("0000000000005410EC03615000090000000000262F4A".toHexByteArray())
        assertThat(tc).isNotNull
        assertThat(tc.sender).isEqualTo("000000000000")
        assertThat(tc.receiver).isEqualTo("5410EC036150")
        val pack = tc.pack
        assertThat(pack.tag).isEqualTo(0)
        assertThat(pack.token).isEqualTo("00000000")
        assertThat(pack.command).isEqualTo(Command.GET_NAME)
        assertThat(pack.checksum).describedAs("Correct pack checksum").isEqualTo(pack.calculatedChecksum)
        assertThat(tc.checksum).describedAs("Correct tc checksum").isEqualTo(tc.calculatedChecksum)
    }

    @Test
    fun fromByteArrayGetNameResponse() {
        val tc = TransportContainer.from("5410EC03615000000000000600180100000000A64269536563757220476174657761795E97".toHexByteArray())
        assertThat(tc).isNotNull
        assertThat(tc.sender).isEqualTo("5410EC036150")
        assertThat(tc.receiver).isEqualTo("000000000006")
        val pack = tc.pack
        assertThat(pack.tag).isEqualTo(1)
        assertThat(pack.token).isEqualTo("00000000")
        assertThat(pack.command).isEqualTo(Command.GET_NAME)
        assertThat(pack.checksum).describedAs("Correct pack checksum").isEqualTo(pack.calculatedChecksum)
        assertThat(tc.checksum).describedAs("Correct tc checksum").isEqualTo(tc.calculatedChecksum)
    }
}
