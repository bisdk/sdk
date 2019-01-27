class BisecureAuth {

    fun createAuth(appId: String = "000000000000", gatewayId: String, userName: String, password: String): String {
        val bodyLength = encodeInt(userName.length + password.length + 10)
        val userNameLength = encodeInt(userName.length)
        val userNameHex = encodeString(userName)
        val passwordHex = encodeString(password)
        return "${appId}${gatewayId}00${bodyLength}000000000010${userNameLength}${userNameHex}${passwordHex}2DF0"
    }

}
