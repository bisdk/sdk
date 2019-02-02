import com.fasterxml.jackson.annotation.JsonProperty

data class Group(
        @field:JsonProperty("ID")
        val id: Int,
        @field:JsonProperty("NAME")
        val name: String,
        @field:JsonProperty("PORTS")
        val ports: List<Port>
)

data class Port(
        @field:JsonProperty("ID")
        val id: Int,
        @field:JsonProperty("TYPE")
        val type: Int
)
