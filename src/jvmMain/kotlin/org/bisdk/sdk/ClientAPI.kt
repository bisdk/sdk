package org.bisdk.sdk

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

    fun getName(): String {
        client.sendMessage(BiPackage(Command.GET_NAME))
        return client.readAnswer().payload.getContentAsString()
    }

    fun ping(): String {
        client.sendMessage(BiPackage(Command.PING))
        return client.readAnswer().payload.getContentAsString()
    }

    fun login(userName: String, password: String): Boolean {
        client.sendMessage(
            BiPackage(
                command = Command.LOGIN,
                payload = Payload.login(userName, password)
            )
        )
        val answer = client.readAnswer()
        if (answer.command == Command.LOGIN) {
            return true
        }
        return false
    }

    fun getToken(userName: String, password: String): String {
        if (login(userName, password)) {
            return client.token
        }
        throw AuthenticationException("Could not login")
    }

    fun logout() {
        client.sendMessage(
            BiPackage(
                command = Command.LOGOUT,
                payload = Payload.empty()
            )
        )
        val answer = client.readAnswer()
    }

    /**
     * The getState command returns a map of port and some kind of number. For now I don't know how to handle that
     */
    fun getState(): HashMap<String, Int> {
        client.sendMessage(
            BiPackage(
                command = Command.JMCP,
                payload = Payload.getValues()
            )
        )
        val answer = client.readAnswer()
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    /**
     * The groups are the paired devices. This call returns all devices known to the GW
     */
    fun getGroups(): List<Group> {
        client.sendMessage(
            BiPackage(
                command = Command.JMCP,
                payload = Payload.getGroups()
            )
        )
        val answer = client.readAnswer()
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    /**
     * This will return only the devices that are paired with the current user. We probably never will need this.
     */
    fun getGroupsForUser(): List<Group> {
        client.sendMessage(
            BiPackage(
                command = Command.JMCP,
                payload = Payload.getGroupsForUser()
            )
        )
        val answer = client.readAnswer()
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    /**
     * Triggers an action on the device. For the garage door this means open/close the door (like pressing a button on the hand held)
     */
    fun setState(port: Port): BiPackage {
        client.sendMessage(
            BiPackage(
                command = Command.SET_STATE,
                payload = Payload.setState(port.id)
            )
        )
        return client.readAnswer()
    }

    /**
     * Returns the current state of the port. You can see how much open it is or if it is still running.
     */
    fun getTransition(port: Port): Transition {
        client.sendMessage(
            BiPackage(
                command = Command.HM_GET_TRANSITION,
                payload = Payload.getTransition(port.id)
            )
        )
        val answer: BiPackage = client.readAnswer()
        return Transition.from(answer.payload.toByteArray())
    }

}
