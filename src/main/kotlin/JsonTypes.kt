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
) {
    override fun toString(): String {
        return "Port(id=$id, type=$type (${PortType.from(type)}))"
    }
}

enum class PortType(val code: Int) {
    NONE(0),
    IMPULS(1),
    AUTO_CLOSE(2),
    ON_OFF(3),
    UP(4),
    DOWN(5),
    HALF(6),
    WALK(7),
    LIGHT(8),
    ON(9),
    OFF(10),
    LOCK(11),
    UNLOCK(12),
    OPEN_DOOR(13),
    LIFT(14),
    SINK(15);

    companion object {
        fun from(code: Int): PortType? = values().firstOrNull { it.code == code }
    }
}
