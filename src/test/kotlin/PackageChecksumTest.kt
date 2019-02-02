import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class PackageChecksumTest {

    @Test
    fun calculate() {
        val testPackage = Package(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc"))
        val checksum = PackageChecksum(testPackage).calculate()
        Assertions.assertThat(checksum.toByte().toHexString()).isEqualToIgnoringCase("2d")
    }
}
