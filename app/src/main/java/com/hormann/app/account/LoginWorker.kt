package com.hormann.app.account

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.thomasletsch.Client
import de.thomasletsch.ClientAPI
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


class LoginWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @SuppressLint("HardwareIds")
    override fun doWork(): Result {

        var success = false

        val address: InetAddress = InetAddress.getByName(inputData.getString(KEY_HOST))
        val gatewayId: String = inputData.getString(KEY_GATEWAY_ID).toString()
        val username: String = inputData.getString(KEY_USER_ID).toString()
        val password: String = inputData.getString(KEY_USER_PASSWORD).toString()

        val macAddress: String = getMacAddress().replace(":", "")

        val client = Client(address, macAddress, gatewayId)
        val clientAPI = ClientAPI(client, username, password)
        try {
            success = clientAPI.login()
        } catch (e: Exception) {
            Log.e("LOGIN", "could not login", e)
        } finally {
            clientAPI.logout()
        }

        val outputData = Data.Builder()
            .putBoolean(KEY_RESULT, success)
            .build()

        return Result.success(outputData)
    }


    private fun getMacAddress(): String {
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
                return res1.toString()
            }
        } catch (ex: Exception) {
        }

        return "02:00:00:00:00:00"
    }

    companion object {
        const val KEY_RESULT: String = "KEY_RESULT"
        const val KEY_HOST: String = "KEY_HOST"
        const val KEY_GATEWAY_ID: String = "KEY_GATEWAY_ID"
        const val KEY_USER_ID: String = "userId"
        const val KEY_USER_PASSWORD: String = "userPassword"
    }

}