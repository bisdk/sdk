import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class PackageTest {

    @Test
    fun toByteArray() {
        val testPackage = Package(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc"))
        val byteArray = testPackage.toByteArray()
        val message = byteArray.joinToString(separator = "") { encodeInt(it.toInt()) }
        Assertions.assertThat(message).isEqualTo("00190000000000100674686F6D61736161616262626363632D")
    }
}
