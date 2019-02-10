package com.hormann.app

import android.accounts.AccountManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hormann.app.account.HormannAccountAuthenticator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.login).setOnClickListener {
            AccountManager.get(this).addAccount(HormannAccountAuthenticator.ACCOUNT_TYPE, HormannAccountAuthenticator.TOKEN_TYPE_GATEWAY, null, null, this, null, null)
        }
    }

    override fun onResume() {
        super.onResume()
        val accountManager = AccountManager.get(this)
        val accounts = accountManager.getAccountsByType(HormannAccountAuthenticator.ACCOUNT_TYPE)
        if (accounts.isEmpty()) {
            findViewById<TextView>(R.id.welcome).text = getString(R.string.please_login)
        } else {
            findViewById<TextView>(R.id.welcome).text = getString(R.string.welcome, accounts[0].name)
        }
    }
}
