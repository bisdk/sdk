package org.bisdk

enum class BiError(val code: Int) {
    COMMAND_NOT_FOUND(0),
    INVALID_PROTOCOL(1),
    LOGIN_FAILED(2),
    INVALID_TOKEN(3),
    USER_ALREADY_EXISTS(4),
    NO_EMPTY_USER_SLOT(5),
    INVALID_PASSWORD(6),
    INVALID_USERNAME(7),
    USER_NOT_FOUND(8),
    PORT_NOT_FOUND(9),
    PORT_ERROR(10),
    GATEWAY_BUSY(11),
    PERMISSION_DENIED(12),
    NO_EMPTY_GROUP_SLOT(13),
    GROUP_NOT_FOUND(14),
    INVALID_PAYLOAD(15),
    OUT_OF_RANGE(16),
    ADD_PORT_ERROR(17),
    NO_EMPTY_PORT_SLOT(18),
    ADAPTER_BUSY(19);

    companion object {
        fun from(code: Int): BiError? = values().firstOrNull { it.code == code }
    }
}
