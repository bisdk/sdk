package org.bisdk

/**
 * Certain fixed lengths in the MCP message
 */
class Lengths {
    companion object {
        val BYTE_LENGTH = 2
        val ADDRESS_BYTES = 6
        val ADDRESS_SIZE = ADDRESS_BYTES * BYTE_LENGTH
        val LENGTH_BYTES = 2
        val LENGTH_SIZE = LENGTH_BYTES * BYTE_LENGTH
        val TAG_BYTES = 1
        val TAG_SIZE = TAG_BYTES * BYTE_LENGTH
        val TOKEN_BYTES = 4
        val TOKEN_SIZE = TOKEN_BYTES * BYTE_LENGTH
        val COMMAND_BYTES = 1
        val COMMAND_SIZE = COMMAND_BYTES * BYTE_LENGTH
        val CHECKSUM_SIZE = 1 * BYTE_LENGTH
        val FRAME_SIZE = LENGTH_SIZE + TAG_SIZE + TOKEN_SIZE + COMMAND_SIZE + CHECKSUM_SIZE
    }
}
