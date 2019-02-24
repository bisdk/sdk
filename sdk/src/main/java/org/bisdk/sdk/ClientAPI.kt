package org.bisdk.sdk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

class ClientAPI(
    val client: Client
) {

    fun getName(): String {
        client.sendMessage(Package(Command.Companion.GET_NAME))
        return client.readAnswer().payload.getContentAsString()
    }

    fun ping(): String {
        client.sendMessage(Package(Command.Companion.PING))
        return client.readAnswer().payload.getContentAsString()
    }

    fun login(userName: String, password: String): Boolean {
        client.sendMessage(
            Package(
                command = Command.Companion.LOGIN,
                payload = Payload.Companion.login(userName, password)
            )
        )
        val answer = client.readAnswer()
        if (answer.command == Command.Companion.LOGIN) {
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
            Package(
                command = Command.Companion.LOGOUT,
                payload = Payload.Companion.empty()
            )
        )
        val answer = client.readAnswer()
    }

    /**
     * The getState command returns a map of port and some kind of number. For now I don't know how to handle that
     */
    fun getState(): HashMap<String, Int> {
        client.sendMessage(
            Package(
                command = Command.Companion.JMCP,
                payload = Payload.Companion.getValues()
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
            Package(
                command = Command.Companion.JMCP,
                payload = Payload.Companion.getGroups()
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
            Package(
                command = Command.Companion.JMCP,
                payload = Payload.Companion.getGroupsForUser()
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
    fun setState(port: Port): Package {
        client.sendMessage(
            Package(
                command = Command.Companion.SET_STATE,
                payload = Payload.Companion.setState(port.id)
            )
        )
        return client.readAnswer()
    }

    /**
     * Returns the current state of the port. You can see how much open it is or if it is still running.
     */
    fun getTransition(port: Port): Transition {
        client.sendMessage(
            Package(
                command = Command.Companion.HM_GET_TRANSITION,
                payload = Payload.Companion.getTransition(port.id)
            )
        )
        val answer: Package = client.readAnswer()
        return Transition.Companion.from(answer.payload.toByteArray())
    }

}
