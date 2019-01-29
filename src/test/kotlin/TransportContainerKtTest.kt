
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TransportContainerKtTest {

    @Test
    fun testEncodeHexString() {
        Assertions.assertThat("10".parseHexToByteArray()).contains(16)
        Assertions.assertThat("2e".parseHexToByteArray()).contains(0x2e)
        Assertions.assertThat("FF".parseHexToByteArray()).contains(0xff)
    }

    @Test
    fun testParseHexToByteArrayLong() {
        Assertions.assertThat("5410EC036150".parseHexToByteArray()).containsExactly(0x54, 0x10, 0xec, 0x03, 0x61, 0x50)
    }

    @Test
    fun toByteArrayLoginPackage() {
        val expected = "0000000000005410EC03615000190000000000100674686F6D61736161616262626363632DF0".toByteArray()
        val tc = TransportContainer("000000000000", "5410EC036150", Package(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc")))
        assertThat(tc.toByteArray()).isEqualTo(expected)
    }
}
