package org.bisdk.sdk

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.bisdk.AuthenticationException
import org.bisdk.BiError
import org.bisdk.Command
import org.bisdk.Payload
import java.net.SocketException

/**
 * The ClientAPI is the high-level API for sending commands to the BiSecure Gateway.
 * It is the preferred way to use the BiSDK library.
 *
 * A ClientAPI needs a GatewayConnection to work. This which can be retrieved / initialized with the result of the discovery.
 *
 * Example usage:
 *
 *      val discovery = Discovery()
 *      val future = discovery.startServer()
 *      discovery.sendDiscoveryRequest()
 *      val discoveryData = future.join()
 *      val client = GatewayConnection(address = discoveryData.sourceAddress, gatewayId =  discoveryData.getGatewayId())
 *      val clientAPI = ClientAPI(client)
 *
 * Most of the commands need you to authorize first:
 *      clientAPI.login("username", "password")
 *  This will create a session and the token will be automatically used for the next commands.
 *  After you are done, you can logout again:
 *      clientAPI.logout()
 */
class ClientAPI(
    val gatewayConnection: GatewayConnection
) : AutoCloseable {
    private var userName: String? = null
    private var password: String? = null
    private var tag = 0

    suspend fun getName(): String {
        val answer = sendWithRetry(BiPackage.fromCommandAndPayload(Command.GET_NAME, Payload.empty()))
        return answer.payload.getContentAsString()
    }

    suspend fun ping(): Boolean {
        return try {
            val answer = sendDirectly(BiPackage.fromCommandAndPayload(Command.PING, Payload.empty()))
            answer.command == Command.PING
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(userName: String, password: String): Boolean {
        var i = 3
        while (i-- > 0) {
            val answer = sendWithRetry(
                BiPackage.fromCommandAndPayload(
                    command = Command.LOGIN,
                    payload = Payload.login(userName, password)
                )
            )
            if (answer.command == Command.LOGIN) {
                this.userName = userName
                this.password = password
                return true
            } else {
                Logger.info("Received " + answer.command + " and not LOGIN as expected => retrying...")
                Thread.sleep(500)
            }
        }
        return false
    }

    suspend fun relogin() {
        if (userName == null || password == null) {
            return
        }
        val newTag = getNewTag()
        gatewayConnection.sendMessage(
            BiPackage.fromCommandAndPayload(
                command = Command.LOGIN,
                payload = Payload.login(userName!!, password!!)
            ).copy(tag = newTag)
        )
        gatewayConnection.readAnswer(newTag)
    }

    suspend fun getToken(userName: String, password: String): String {
        if (login(userName, password)) {
            return gatewayConnection.token
        }
        throw AuthenticationException("Could not login")
    }

    suspend fun logout() {
        // We do not send logout with retry since it could be called when something is disposed after some time
        // Chances are very high that the connection is already gone
        gatewayConnection.sendMessage(
            BiPackage.fromCommandAndPayload(
                command = Command.LOGOUT,
                payload = Payload.empty()
            ).copy(tag = getNewTag())
        )
    }

    /**
     * The getState command returns a map of port and some kind of number. For now I don't know how to handle that
     */
    suspend fun getState(): HashMap<String, Int> {
        var i = 3
        while (i-- > 0) {
            val answer = sendWithRetry(
                BiPackage.fromCommandAndPayload(
                    command = Command.JMCP,
                    payload = Payload.getValues()
                )
            )
            val json = answer.payload.getContentAsString()
            val mapper = ObjectMapper()
            mapper.registerModules(KotlinModule(), ParameterNamesModule())
            try {
                return mapper.readValue(json)
            } catch (e: JsonProcessingException) {
                Logger.info("Could not deserialize Groups from $json, error: " + e.message + " -> retrying...")
                Thread.sleep(500)
            }
        }
        throw IllegalStateException("Retry failed, got error answer! ")
    }

    /**
     * The groups are the paired devices. This call returns all devices known to the GW
     */
    suspend fun getGroups(): List<Group> {
        val answer = sendWithRetry(
            BiPackage.fromCommandAndPayload(
                command = Command.JMCP,
                payload = Payload.getGroups()
            )
        )
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        try {
            return mapper.readValue(json)
        } catch (e: JsonProcessingException) {
            Logger.info("Could not deserialize Groups from $json, error: " + e.message + " -> retrying...")
            throw e
        }
    }

    /**
     * This will return only the devices that are paired with the current user. We probably should always use this
     */
    suspend fun getGroupsForUser(): List<Group> {
        val answer = sendWithRetry(
            BiPackage.fromCommandAndPayload(
                command = Command.JMCP,
                payload = Payload.getGroupsForUser()
            )
        )
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        try {
            return mapper.readValue(json)
        } catch (e: JsonProcessingException) {
            Logger.info("Could not deserialize Groups from $json, error: " + e.message + " -> retrying...")
            throw e
        }
    }

    /**
     * Triggers an action on the device. For the garage door this means open/close the door (like pressing a button on the hand held)
     */
    suspend fun setState(port: Port): BiPackage {
        return sendWithRetry(
            BiPackage.fromCommandAndPayload(
                command = Command.SET_STATE,
                payload = Payload.setState(port.id)
            )
        )
    }

    /**
     * Returns the current state of the port. You can see how much open it is or if it is still running.
     */
    suspend fun getTransition(port: Port): Transition {
        val answer: BiPackage = sendWithRetry(
            BiPackage.fromCommandAndPayload(
                command = Command.HM_GET_TRANSITION,
                payload = Payload.getTransition(port.id)
            )
        )
        return Transition.from(answer.payload.toByteArray())
    }

    /**
     * Central method to handle all error conditions wile sending and receiving data from the gateway.
     *
     * It will retry certain times and try to handle special error conditions with a good retry / reconnect / relogin strategy
     */
    private suspend fun sendWithRetry(messageToSend: BiPackage): BiPackage {
        var message = messageToSend.copy(tag = getNewTag())
        var sendCounter = 3L
        while (sendCounter-- > 0) {
            try {
                gatewayConnection.sendMessage(message)
                break
            } catch (e: Exception) {
                Logger.info("Received Exception ${e.message} => reconnecting...")
                gatewayConnection.reconnect()
                relogin()
                Thread.sleep((4 - sendCounter) * 100) // Increase waiting time for each retry
            }
        }
        var error = "N/A"
        var i = 2L
        while (i-- > 0) {
            try {
                val tc = gatewayConnection.readAnswer(message.tag)
                val command = tc.pack.command
                if (tc.pack.command == Command.ERROR && tc.pack.getBiError() != null && tc.pack.getBiError() == BiError.PERMISSION_DENIED) {
                    Logger.debug("Received PERMISSION_DENIED => relogin...")
                    relogin()
                    message = messageToSend.copy(tag = getNewTag())  // try with new tag to see in log file which message was answered
                    gatewayConnection.sendMessage(message)
                    Thread.sleep(500)
                } else if (command == Command.ERROR) {
                    Logger.debug("Received ERROR (" + tc.pack.getBiError() + ") answer => retrying...")
                    error = "Received ERROR answer"
                    message = messageToSend.copy(tag = getNewTag())  // try with new tag to see in log file which message was answered
                    gatewayConnection.sendMessage(message)
                    Thread.sleep(500)
                } else if (command == Command.EMPTY && i == 1L) {
                    Logger.info("Received EMPTY answer 2 times => reconnecting...")
                    error = "Received EMPTY answer"
                    gatewayConnection.reconnect()
                    relogin()
                    message = messageToSend.copy(tag = getNewTag())  // try with new tag to see in log file which message was answered
                    gatewayConnection.sendMessage(message)
                    Thread.sleep(500)
                } else if (command == Command.EMPTY) {
                    Logger.debug("Received EMPTY answer => waiting...")
                    error = "Received EMPTY answer"
                    Thread.sleep(500)
                } else {
                    return tc.pack
                }
            } catch (e: SocketException) {
                Logger.info("Received SocketException ${e.message} => reconnecting...")
                gatewayConnection.reconnect()
                relogin()
                message = messageToSend.copy(tag = getNewTag())  // try with new tag to see in log file which message was answered
                gatewayConnection.sendMessage(message)
                Thread.sleep(500)
            } catch (e: IllegalStateException) {
                Logger.debug(
                    "Received Exception ${e.message} (which probably comes from a timeout while reading). " +
                            "This happens after a while (about 2min), the GW just stops responding => retrying..."
                )
                error = "Received Exception ${e.message}"
                gatewayConnection.reconnect()
                relogin()
                message = messageToSend.copy(tag = getNewTag())  // try with new tag to see in log file which message was answered
                gatewayConnection.sendMessage(message)
            } catch (e: Exception) {
                Logger.info("Received Exception ${e.message} => retrying...")
                error = "Received Exception ${e.message}"
                Thread.sleep(500)
            }

        }
        throw IllegalStateException("Retry failed: $error")
    }

    /**
     * Send without retry and error handling
     */
    private suspend fun sendDirectly(messageToSend: BiPackage): BiPackage {
        val message = messageToSend.copy(tag = getNewTag())
        gatewayConnection.sendMessage(message)
        val tc = gatewayConnection.readAnswer(message.tag)
        return tc.pack
    }

    private fun getNewTag(): Int {
        if (tag >= 128) {
            tag = 0
        } else {
            tag++
        }
        return tag
    }

    override fun close() {
        gatewayConnection.close()
    }

}
