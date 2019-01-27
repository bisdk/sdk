import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class ChecksumTest {

    @Test
    fun calculate() {
        val testPackage = Package(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc"))
        val checksum = Checksum(testPackage).calculate()
        Assertions.assertThat(encodeInt(checksum)).isEqualTo("2d")
    }
}
