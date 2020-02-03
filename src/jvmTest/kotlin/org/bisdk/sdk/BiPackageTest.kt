package org.bisdk.sdk

import org.assertj.core.api.Assertions
import org.bisdk.Command
import org.bisdk.Payload
import org.bisdk.toHexString
import org.junit.Test
import java.util.*

internal class BiPackageTest {

    @Test
    fun testLogin() {
        val testPackage = BiPackage.login("thomas", "aaabbbccc")
        val byteArray = testPackage.toByteArray()
        val message = byteArray.toHexString()
        Assertions.assertThat(message).isEqualTo("00190000000000100674686F6D61736161616262626363632D")
    }

    @Test
    fun testGetName() {
        val testPackage = BiPackage.fromCommandAndPayload(command = Command.GET_NAME, payload = Payload.empty())
        val byteArray = testPackage.toByteArray()
        val message = byteArray.toHexString()
        Assertions.assertThat(message).isEqualTo("00090000000000262F")
    }

    @Test
    fun testJMCP() {
        val testPackage = BiPackage.jmcp("{\"cmd\":\"GET_VALUES\"}")
        val byteArray = testPackage.toByteArray()
        val message = byteArray.toHexString()
        Assertions.assertThat(message).isEqualTo("001D0000000000067B22636D64223A224745545F56414C554553227D20")
    }

    @Test
    fun testGetCommandCode() {
        val testPackage = BiPackage.fromCommandAndPayload(command = Command.GET_NAME, payload = Payload.empty())
        Assertions.assertThat(testPackage.getCommandCode()).isEqualTo(Command.GET_NAME.code.toByte())
    }

    @Test
    fun testGetCommandCodeForResponse() {
        val testPackage = BiPackage(command = Command.GET_NAME, payload = Payload.empty(), isResponse = true)
        Assertions.assertThat(BitSet.valueOf(listOf(testPackage.getCommandCode()).toByteArray()).get(7)).describedAs("Bit #7 should be set").isTrue()
    }
}
