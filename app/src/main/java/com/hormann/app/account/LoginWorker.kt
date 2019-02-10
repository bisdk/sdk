package com.hormann.app.account

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class LoginWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        Thread.sleep(500)

        val outputData = Data.Builder()
                .putBoolean(KEY_RESULT, true)
                .build()

        return Result.success(outputData)
    }

    companion object {
        const val KEY_RESULT: String = "KEY"
        const val KEY_HOST: String = "KEY"
        const val KEY_USER_ID: String = "userId"
        const val KEY_USER_PASSWORD: String = "userPassword"
    }

}