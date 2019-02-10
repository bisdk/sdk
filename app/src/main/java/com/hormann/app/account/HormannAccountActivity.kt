package com.hormann.app.account

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.hormann.app.R

class HormannAccountActivity : AccountAuthenticatorActivity() {

    companion object {
        private const val REQ_REGISTER = 11
    }

    private var accountManager: AccountManager? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.account_login)
        findViewById<Button>(R.id.login).setOnClickListener { login() }
        accountManager = AccountManager.get(baseContext)
    }

    fun createAccount(view: View) {
        val intent = Intent(baseContext, HormannCreateAccountActivity::class.java)
        intent.putExtras(getIntent().extras!!)
        startActivityForResult(intent, REQ_REGISTER)
    }

    private fun login() {
        val userId = (findViewById<View>(R.id.user) as EditText).text.toString()
        val passWd = (findViewById<View>(R.id.password) as EditText).text.toString()

        val accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)

        object : AsyncTask<Void, Void, Intent>() {
            override fun doInBackground(vararg params: Void): Intent {
                val data = Bundle()

                val authToken = HormannAccountRegLoginHelper.authenticate(userId, passWd)
                val tokenType = HormannAccountRegLoginHelper.getTokenType(userId)

                data.putString(AccountManager.KEY_ACCOUNT_NAME, userId)
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
                data.putString(HormannAccountAuthenticator.TOKEN_TYPE, tokenType)
                data.putString(AccountManager.KEY_AUTHTOKEN, authToken)
                data.putString(HormannAccountAuthenticator.PASSWORD, passWd)

                val result = Intent()
                result.putExtras(data)

                return result
            }

            override fun onPostExecute(intent: Intent) {
                setLoginResult(intent)
            }
        }.execute()
    }

    private fun setLoginResult(intent: Intent) {

        val userId = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val passWd = intent.getStringExtra(HormannAccountAuthenticator.PASSWORD)

        val account = Account(userId, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))

        if (getIntent().getBooleanExtra(HormannAccountAuthenticator.ADD_ACCOUNT, false)) {
            val authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN)
            val tokenType = intent.getStringExtra(HormannAccountAuthenticator.TOKEN_TYPE)

            accountManager!!.addAccountExplicitly(account, passWd, null)
            accountManager!!.setAuthToken(account, tokenType, authToken)
        } else {
            accountManager!!.setPassword(account, passWd)
        }

        setAccountAuthenticatorResult(intent.extras)
        setResult(AppCompatActivity.RESULT_OK, intent)

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQ_REGISTER) {
            setLoginResult(data)
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}