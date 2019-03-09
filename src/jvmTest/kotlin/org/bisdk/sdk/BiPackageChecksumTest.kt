package org.bisdk.sdk

import org.assertj.core.api.Assertions
import org.bisdk.Command
import org.junit.Test

internal class BiPackageChecksumTest {

    @Test
    fun calculate() {
        val testPackage = BiPackage(command = Command.LOGIN, payload = Payload.login("thomas", "aaabbbccc"))
        val checksum = PackageChecksum(testPackage).calculate()
        Assertions.assertThat(checksum.toByte().toHexString()).isEqualToIgnoringCase("2d")
    }
}
