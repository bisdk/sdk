package de.thomasletsch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

class ClientAPI(private val client: Client,
                private val userName: String, private val password: String) {

    fun getName(): String {
        client.sendMessage(Package(Command.GET_NAME))
        return client.readAnswer().payload.getContentAsString()
    }

    fun ping(): String {
        client.sendMessage(Package(Command.PING))
        return client.readAnswer().payload.getContentAsString()
    }

    fun login() {
        // We retry login because sometime we get a LOGOUT when another client (the android app) is also logged in.
        // It seems that only one client can be logged in at the same time.
        val maxRetries = 5
        var retries = 0
        do {
            client.token = "00000000"  // reset the token before login
            client.sendMessage(Package(command = Command.LOGIN, payload = Payload.login(userName, password)))
            val answer = client.readAnswer()
            if(answer.command == Command.LOGOUT) {
                logout()
            }
            retries++
            Thread.sleep(100)
        } while (retries < maxRetries && answer.command != Command.LOGIN)
    }

    fun logout() {
        client.sendMessage(Package(command = Command.LOGOUT, payload = Payload.empty()))
        val answer = client.readAnswer()
    }

    /**
     * The getState command returns a map of port and some kind of number. For now I don't know how to handle that
     */
    fun getState(): HashMap<String, Int> {
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.getValues()))
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
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.getGroups()))
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
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.getGroupsForUser()))
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
        client.sendMessage(Package(command = Command.SET_STATE, payload = Payload.setState(port.id)))
        return client.readAnswer()
    }

    /**
     * Returns the current state of the port. You can see how much open it is or if it is still running.
     */
    fun getTransition(port: Port): Transition {
        val maxRetries = 5
        var retries = 0
        var answer: Package
        do {
            client.sendMessage(Package(command = Command.HM_GET_TRANSITION, payload = Payload.getTransition(port.id)))
            answer = client.readAnswer()
            retries++
            Thread.sleep(100)
            if(retries > 2) {
                // If we get an error more than one time, it could be that we got logged out => login and try again
                logout()
                login()
            }
        } while (retries < maxRetries && answer.command != Command.HM_GET_TRANSITION)
        return Transition.from(answer.payload.toByteArray())
    }

}
