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

    fun login() {
        // We retry login because sometime we get a LOGOUT when another client (the android app) is also logged in.
        // It seems that only one client can be logged in at the same time.
        val maxRetries = 5
        var retries = 0
        do {
            client.sendMessage(Package(command = Command.LOGIN, payload = Payload.login(userName, password)))
            val answer = client.readAnswer()
            retries++
            Thread.sleep(100)
        } while (retries < maxRetries && answer.command != Command.LOGIN)
    }

    fun getState(): HashMap<String, Int> {
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.getValues()))
        val answer = client.readAnswer()
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    fun getGroupsForUser(): List<Group> {
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.getGroupsForUser()))
        val answer = client.readAnswer()
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        return mapper.readValue(json)
    }

    fun setState(port: Port): Package {
        client.sendMessage(Package(command = Command.SET_STATE, payload = Payload.setState(port.id)))
        return client.readAnswer()
    }

    fun getTransition(port: Port): Transition {
        val maxRetries = 5
        var retries = 0
        var answer: Package
        do {
            client.sendMessage(Package(command = Command.HM_GET_TRANSITION, payload = Payload.getTransition(port.id)))
            answer = client.readAnswer()
            Thread.sleep(100)
        } while (retries < maxRetries && answer.command != Command.HM_GET_TRANSITION)
        return Transition.from(answer.payload.toByteArray())
    }

}
