package org.bisdk.sdk

import org.assertj.core.api.Assertions
import org.junit.Test

internal class PackageChecksumTest {

    @Test
    fun calculate() {
        val testPackage = Package(command = org.bisdk.sdk.Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc"))
        val checksum = PackageChecksum(testPackage).calculate()
        Assertions.assertThat(checksum.toByte().toHexString()).isEqualToIgnoringCase("2d")
    }
}
