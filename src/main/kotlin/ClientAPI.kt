
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
        client.sendMessage(Package(command = Command.LOGIN, payload = Payload.login(userName, password)))
        client.readAnswer()
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
        client.sendMessage(Package(command = Command.JMCP, payload = Payload.setState(port.id)))
        val answer = client.readAnswer()
        return answer
    }

}
