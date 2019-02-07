package com.hormann.app.discover

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hormann.app.BuildConfig
import de.thomasletsch.Discovery

class DiscoverWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        val userDao = AppDatabase.getInstance(applicationContext).userDao()

        userDao.clear()

        if (BuildConfig.DEBUG) {
            // Debugger cannot scan network, manually add gateways here
            userDao.insertAll(Gateway("5410ECD7ECD6", "10.0.1.106", 4000))
        }
        val discovery = Discovery()
        discovery.start().thenApplyAsync {
            userDao.insertAll(Gateway(it.getGatewayId(), it.sourceAddress.hostAddress, 4000))
        }.join()
        return Result.success()
    }

}