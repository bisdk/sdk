package org.bisdk.sdk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

class ClientAPI(
    val client: org.bisdk.sdk.Client
) {

    fun getName(): String {
        client.sendMessage(org.bisdk.sdk.Package(org.bisdk.sdk.Command.Companion.GET_NAME))
        return client.readAnswer().payload.getContentAsString()
    }

    fun ping(): String {
        client.sendMessage(org.bisdk.sdk.Package(org.bisdk.sdk.Command.Companion.PING))
        return client.readAnswer().payload.getContentAsString()
    }

    fun login(userName: String, password: String): Boolean {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.LOGIN,
                payload = org.bisdk.sdk.Payload.Companion.login(userName, password)
            )
        )
        val answer = client.readAnswer()
        if (answer.command == org.bisdk.sdk.Command.Companion.LOGIN) {
            return true
        }
        return false
    }

    fun getToken(userName: String, password: String): String {
        if (login(userName, password)) {
            return client.token
        }
        throw org.bisdk.sdk.AuthenticationException("Could not login")
    }

    fun logout() {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.LOGOUT,
                payload = org.bisdk.sdk.Payload.Companion.empty()
            )
        )
        val answer = client.readAnswer()
    }

    /**
     * The getState command returns a map of port and some kind of number. For now I don't know how to handle that
     */
    fun getState(): HashMap<String, Int> {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.JMCP,
                payload = org.bisdk.sdk.Payload.Companion.getValues()
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
    fun getGroups(): List<org.bisdk.sdk.Group> {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.JMCP,
                payload = org.bisdk.sdk.Payload.Companion.getGroups()
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
    fun getGroupsForUser(): List<org.bisdk.sdk.Group> {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.JMCP,
                payload = org.bisdk.sdk.Payload.Companion.getGroupsForUser()
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
    fun setState(port: org.bisdk.sdk.Port): org.bisdk.sdk.Package {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.SET_STATE,
                payload = org.bisdk.sdk.Payload.Companion.setState(port.id)
            )
        )
        return client.readAnswer()
    }

    /**
     * Returns the current state of the port. You can see how much open it is or if it is still running.
     */
    fun getTransition(port: org.bisdk.sdk.Port): org.bisdk.sdk.Transition {
        client.sendMessage(
            org.bisdk.sdk.Package(
                command = org.bisdk.sdk.Command.Companion.HM_GET_TRANSITION,
                payload = org.bisdk.sdk.Payload.Companion.getTransition(port.id)
            )
        )
        val answer: org.bisdk.sdk.Package = client.readAnswer()
        return org.bisdk.sdk.Transition.Companion.from(answer.payload.toByteArray())
    }

}
