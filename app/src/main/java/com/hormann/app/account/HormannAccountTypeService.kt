package com.hormann.app.account

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HormannAccountTypeService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        val authenticator = HormannAccountAuthenticator(this)
        return authenticator.iBinder
    }
}