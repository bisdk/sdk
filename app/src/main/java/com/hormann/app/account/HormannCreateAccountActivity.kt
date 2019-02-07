package com.hormann.app.account

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import com.hormann.app.R

class HormannCreateAccountActivity : AppCompatActivity() {

    private var accountType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)

    }

    fun createAccount(view: View) {
        val userId = (findViewById<View>(R.id.user) as EditText).text.toString()
        val passWd = (findViewById<View>(R.id.password) as EditText).text.toString()
        val name = (findViewById<View>(R.id.name) as EditText).text.toString()

        if (!HormannAccountRegLoginHelper.validateAccountInfo(name, userId, passWd)) {
            (findViewById<View>(R.id.error) as TextView).setText(R.string.enter_valid_information)
        }

        val authToken = HormannAccountRegLoginHelper.createAccount(name, userId, passWd)
        val authTokenType = HormannAccountRegLoginHelper.getTokenType(userId)

        if (authToken.isEmpty()) {
            (findViewById<View>(R.id.error) as TextView).setText(R.string.account_could_not_be_created)
        }

        val data = Bundle()
        data.putString(AccountManager.KEY_ACCOUNT_NAME, userId)
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        data.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        data.putString(HormannAccountAuthenticator.PASSWORD, passWd)
        data.putString(HormannAccountAuthenticator.TOKEN_TYPE, authTokenType)

        val result = Intent()
        result.putExtras(data)

        setResult(Activity.RESULT_OK, result)
        finish()
    }
}