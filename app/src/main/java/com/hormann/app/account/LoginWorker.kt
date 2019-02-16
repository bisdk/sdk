package com.hormann.app.account

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hormann.app.getMacAddress
import de.thomasletsch.AuthenticationException
import de.thomasletsch.Client
import de.thomasletsch.ClientAPI
import java.net.InetAddress


class LoginWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {


        val address: InetAddress = InetAddress.getByName(inputData.getString(KEY_HOST))
        val gatewayId: String = inputData.getString(KEY_GATEWAY_ID).toString()
        val username: String = inputData.getString(KEY_USER_ID).toString()
        val password: String = inputData.getString(KEY_USER_PASSWORD).toString()

        val macAddress: String = getMacAddress()

        val outputData = Data.Builder()

        val client = Client(address, macAddress, gatewayId)
        val clientAPI = ClientAPI(client)
        try {
            clientAPI.getToken(username, password)
            outputData.putString(KEY_TOKEN, client.token)
            outputData.putBoolean(KEY_RESULT, true)
        } catch (e: AuthenticationException) {
            outputData.putBoolean(KEY_RESULT, false)

            Log.e("LOGIN", "could not login", e)
        } finally {
            clientAPI.logout()
        }

        return Result.success(outputData.build())
    }



    companion object {
        const val KEY_RESULT: String = "KEY_RESULT"
        const val KEY_TOKEN: String = "KEY_TOKEN"
        const val KEY_HOST: String = "KEY_HOST"
        const val KEY_GATEWAY_ID: String = "KEY_GATEWAY_ID"
        const val KEY_USER_ID: String = "KEY_USER_ID"
        const val KEY_USER_PASSWORD: String = "KEY_USER_PASSWORD"
    }

}