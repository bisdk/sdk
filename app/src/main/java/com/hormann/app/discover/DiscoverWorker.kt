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

        if (BuildConfig.DEBUG) {
            // Debugger cannot scan network, manually add gateways here
            userDao.insertAll(Gateway("5410ECD7ECD6", "10.0.1.106", 4000))
        }

        val discovery = Discovery()
        try {
            discovery.start().thenApplyAsync {
                userDao.insertAll(Gateway(it.getGatewayId(), it.sourceAddress.hostAddress, 4000))
            }.get(3, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            Log.e("Discovery", "timout", e)
        }


        return Result.success()
    }

}