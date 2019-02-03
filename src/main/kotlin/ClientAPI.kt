
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

class ClientAPI(val client: Client) {

    fun getName(): String {
        client.sendMessage(Package(Command.GET_NAME))
        return client.readAnswer().payload.getContentAsString()
    }

    fun login(userName: String, password: String) {
        val maxRetries = 3
        var retries = 0
        do {
            client.sendMessage(Package(command = Command.LOGIN, payload = Payload.login(userName, password)))
            val answer = client.readAnswer()
            retries++
        } while (retries < maxRetries && answer.command != Command.LOGIN)
    }

    fun getGroupsForUser(): List<Group> {
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.getGroupsForUser()))
        val answer = client.readAnswer()
        val json = answer.payload.getContentAsString()
        val mapper = ObjectMapper()
        mapper.registerModules(KotlinModule(), ParameterNamesModule())
        val groups = mapper.readValue<List<Group>>(json)
        return groups
    }

    fun setState(port: Port): Package {
        client.sendMessage(Package(command = Command.SET_STATE, payload = Payload.setState(port.id)))
        val answer = client.readAnswer()
        return answer
    }

    fun getTransition(port: Port): Transition {
        client.sendMessage(Package(command = Command.HM_GET_TRANSITION, payload = Payload.getTransition(port.id)))
        val answer = client.readAnswer()
        return Transition.from(answer.payload.toByteArray())
    }

}
