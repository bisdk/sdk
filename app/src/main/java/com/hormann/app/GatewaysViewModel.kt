package com.hormann.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hormann.app.discover.AppDatabase
import com.hormann.app.discover.Gateway

class GatewaysViewModel(application: Application) : AndroidViewModel(application) {

    val allPackageTypes: LiveData<List<Gateway>> = AppDatabase.getInstance(application).userDao().getAll()

}