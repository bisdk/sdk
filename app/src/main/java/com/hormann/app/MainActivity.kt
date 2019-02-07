package com.hormann.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hormann.app.discover.DiscoverWorker

class MainActivity : AppCompatActivity() {

    private lateinit var gatewaysViewModel: GatewaysViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WorkManager.getInstance().enqueue(OneTimeWorkRequestBuilder<DiscoverWorker>().build())
        gatewaysViewModel = ViewModelProviders.of(this).get(GatewaysViewModel::class.java)
        initData()
    }


    private fun initData() {
        val packageTypesAdapter = ArrayAdapter<Any>(this@MainActivity, android.R.layout.simple_spinner_item)

        gatewaysViewModel.allPackageTypes.observe(this, Observer { packageTypes ->

            packageTypes?.forEach {
                packageTypesAdapter.add("${it.host}:${it.port}(${it.receiver})")
            }
        })

        findViewById<Spinner>(R.id.gateways).adapter = packageTypesAdapter

    }
}
