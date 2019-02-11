package com.hormann.app.discover

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hormann.app.BuildConfig
import de.thomasletsch.Discovery
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class DiscoverWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        val db = AppDatabase.getInstance(applicationContext)
        val userDao = db.userDao()


        userDao.clear()

        var found = false

        val discovery = Discovery()
        try {
            discovery.start().thenApplyAsync {
                found = true
                userDao.insertAll(Gateway(it.getGatewayId(), it.hwVersion, it.protocol, it.sourceAddress.hostAddress, it.swVersion))
            }.get(3, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            Log.e("Discovery", "timeout", e)
        }


        if (BuildConfig.DEBUG && !found) {
            // Debugger cannot scan network, manually add gateways here
            userDao.insertAll(Gateway("5410ECD7ECD6", "1.0.0", "MCP V3.0", "10.0.1.106", "2.5.0"))
        }

        return Result.success()
    }

}