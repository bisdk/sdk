import org.assertj.core.api.Assertions
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
}
