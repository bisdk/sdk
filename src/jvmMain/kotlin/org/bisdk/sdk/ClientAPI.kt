package org.bisdk.sdk

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.bisdk.AuthenticationException
import org.bisdk.Command
import org.bisdk.Payload

class ClientAPI(
    val client: Client
) {

    private var userName: String? = null
    private var password: String? = null

    fun getName(): String {
        val answer = client.sendWithRetry(BiPackage(Command.GET_NAME))
        return answer.payload.getContentAsString()
    }

    fun ping(): String {
        val answer = client.sendWithRetry(BiPackage(Command.PING))
        return answer.payload.getContentAsString()
    }

    fun login(userName: String, password: String): Boolean {
        var i = 3
        while (i-- > 0) {
            val answer = client.sendWithRetry(
                BiPackage(
                    command = Command.LOGIN,
                    payload = Payload.login(userName, password)
                )
            )
            if (answer.command == Command.LOGIN) {
                this.userName = userName
                this.password = password
                return true
            } else {
                println("Received " + answer.command + " and not LOGIN as expected => retrying...")
                Thread.sleep(500)
            }
        }
        return false
    }

    fun relogin() {
        if (userName == null || password == null) {
            return
        }
        login(userName!!, password!!)
    }

    fun getToken(userName: String, password: String): String {
        if (login(userName, password)) {
            return client.token
        }
        throw AuthenticationException("Could not login")
    }

    fun logout() {
        val answer = client.sendWithRetry(
            BiPackage(
                command = Command.LOGOUT,
                payload = Payload.empty()
            )
        )
    }

    /**
     * The getState command returns a map of port and some kind of number. For now I don't know how to handle that
     */
    fun getState(): HashMap<String, Int> {
        val answer = client.sendWithRetry(
            BiPackage(
                command = Command.JMCP,
                payload = Payload.getValues()
            )
        )
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    /**
     * The groups are the paired devices. This call returns all devices known to the GW
     */
    fun getGroups(): List<Group> {
        var i = 3
        while (i-- > 0) {
            val answer = client.sendWithRetry(
                BiPackage(
                    command = Command.JMCP,
                    payload = Payload.getGroups()
                )
            )
            val json = answer.payload.getContentAsString()
            val mapper = ObjectMapper()
            mapper.registerModules(KotlinModule(), ParameterNamesModule())
            try {
                val value = mapper.readValue<List<Group>>(json)
                return value
            } catch (e: JsonMappingException) {
                println("Could not deserialize Groups from $json, error: " + e.message)
                Thread.sleep(500)
            }
        }
        throw IllegalStateException("Retry failed, got error answer! ")
    }

    /**
     * This will return only the devices that are paired with the current user. We probably never will need this.
     */
    fun getGroupsForUser(): List<Group> {
        val answer = client.sendWithRetry(
            BiPackage(
                command = Command.JMCP,
                payload = Payload.getGroupsForUser()
            )
        )
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    /**
     * Triggers an action on the device. For the garage door this means open/close the door (like pressing a button on the hand held)
     */
    fun setState(port: Port): BiPackage {
        return client.sendWithRetry(
            BiPackage(
                command = Command.SET_STATE,
                payload = Payload.setState(port.id)
            )
        )
    }

    /**
     * Returns the current state of the port. You can see how much open it is or if it is still running.
     */
    fun getTransition(port: Port): Transition {
        val answer: BiPackage = client.sendWithRetry(
            BiPackage(
                command = Command.HM_GET_TRANSITION,
                payload = Payload.getTransition(port.id)
            )
        )
        return Transition.from(answer.payload.toByteArray())
    }

}
