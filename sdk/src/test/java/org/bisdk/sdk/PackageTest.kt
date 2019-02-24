package org.bisdk.sdk

import org.assertj.core.api.Assertions
import org.junit.Test

internal class PackageTest {

    @Test
    fun testLogin() {
        val testPackage = Package.login("thomas", "aaabbbccc")
        val byteArray = testPackage.toByteArray()
        val message = byteArray.toHexString()
        Assertions.assertThat(message).isEqualTo("00190000000000100674686F6D61736161616262626363632D")
    }

    @Test
    fun testGetName() {
        val testPackage = Package(command = org.bisdk.sdk.Command.GET_NAME, payload = Payload.empty())
        val byteArray = testPackage.toByteArray()
        val message = byteArray.toHexString()
        Assertions.assertThat(message).isEqualTo("00090000000000262F")
    }

    @Test
    fun testJMCP() {
        val testPackage = Package.jmcp("{\"cmd\":\"GET_VALUES\"}")
        val byteArray = testPackage.toByteArray()
        val message = byteArray.toHexString()
        Assertions.assertThat(message).isEqualTo("001D0000000000067B22636D64223A224745545F56414C554553227D20")
    }
}
