package com.hormann.app

import java.net.NetworkInterface
import java.util.*

fun getMacAddress(): String {
    var mac = "02:00:00:00:00:00"

    try {
        val all = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (nif in all) {
            if (!nif.name.equals("wlan0", ignoreCase = true)) continue

            val macBytes = nif.hardwareAddress ?: return ""

            val res1 = StringBuilder()
            for (b in macBytes) {
                res1.append(String.format("%02X:", b))
            }

            if (res1.isNotEmpty()) {
                res1.deleteCharAt(res1.length - 1)
            }
            mac = res1.toString()
        }
    } catch (ex: Exception) {
    }

    return mac.replace(":", "")
}