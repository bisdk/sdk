package org.bisdk.sdk

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

enum class GroupType(val code: Int, val ports: List<PortType> = emptyList()) {
    NONE(0),
    SECTIONAL_DOOR(1, listOf(PortType.IMPULS, PortType.UP, PortType.DOWN, PortType.HALF, PortType.LIGHT)),
    HORIZONTAL_SECTIONAL_DOOR(2, listOf(PortType.IMPULS, PortType.UP, PortType.DOWN, PortType.HALF, PortType.LIGHT)),
    SWING_GATE_SINGLE(3, listOf(PortType.IMPULS, PortType.HALF, PortType.UP, PortType.DOWN, PortType.LIGHT)),
    SWING_GATE_DOUBLE(4, listOf(PortType.IMPULS, PortType.WALK, PortType.UP, PortType.DOWN, PortType.LIGHT)),
    SLIDING_GATE(5, listOf(PortType.IMPULS, PortType.UP, PortType.DOWN, PortType.HALF, PortType.LIGHT)),
    DOOR(6, listOf(PortType.AUTO_CLOSE, PortType.IMPULS, PortType.LIGHT, PortType.ON_OFF)),
    LIGHT(7, listOf(PortType.ON, PortType.OFF, PortType.ON_OFF, PortType.IMPULS)),
    OTHER(8, listOf(PortType.ON, PortType.OFF, PortType.ON_OFF, PortType.IMPULS)),
    JACK(9, listOf(PortType.ON, PortType.OFF, PortType.ON_OFF, PortType.IMPULS)),
    SMARTKEY(10, listOf(PortType.LOCK, PortType.UNLOCK, PortType.OPEN_DOOR)),
    PILOMAT_POLLER(11, listOf(PortType.LIFT, PortType.SINK)),
    PILOMAT_DURCHFAHRTSSPERRE(12, listOf(PortType.LIFT, PortType.SINK)),
    PILOMAT_HUBBALKEN(13, listOf(PortType.LIFT, PortType.SINK)),
    PILOMAT_REIFENKILLER(14, listOf(PortType.LIFT, PortType.SINK)),
    BARRIER(15, listOf(PortType.IMPULS, PortType.UP, PortType.DOWN));

    companion object {
        fun from(code: Int): GroupType? = values().firstOrNull { it.code == code }
    }
}
