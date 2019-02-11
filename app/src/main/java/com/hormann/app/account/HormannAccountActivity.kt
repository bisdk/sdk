package com.hormann.app.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.work.*
import com.hormann.app.R
import com.hormann.app.StoreListAdapter
import com.hormann.app.discover.DiscoverWorker


class HormannAccountActivity : AppCompatAccountAuthenticatorActivity() {

    private var accountManager: AccountManager? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_login)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.gateway)
        val storeListAdapter = StoreListAdapter(this, this)
        autoCompleteTextView.setAdapter(storeListAdapter)
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val itemId = storeListAdapter.getItem(position)
            findViewById<EditText>(R.id.gateway).setText(itemId?.sourceAddress)
            findViewById<EditText>(R.id.mac).setText(itemId?.mac)
            Log.d("TEST", parent[0].toString())
        }
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        WorkManager.getInstance().enqueue(
                OneTimeWorkRequestBuilder<DiscoverWorker>().setConstraints(constraints).build()
        )


        findViewById<Button>(R.id.login_in_button).setOnClickListener {
            val host = findViewById<EditText>(R.id.gateway).text.toString()
            val mac = findViewById<EditText>(R.id.mac).text.toString()
            val userId = findViewById<EditText>(R.id.username).text.toString()
            val passWd = findViewById<EditText>(R.id.password).text.toString()

            login(host, userId, passWd, mac.replace(":", "").toUpperCase())
        }
        accountManager = AccountManager.get(baseContext)
    }

    private fun login(host: String, userId: String, passWord: String, gatewayId: String) {
        findViewById<ProgressBar>(R.id.login_progress).visibility = View.VISIBLE
        findViewById<ScrollView>(R.id.login_form).visibility = View.GONE
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val data = Data.Builder()
                .putString(LoginWorker.KEY_GATEWAY_ID, gatewayId)
                .putString(LoginWorker.KEY_HOST, host)
                .putString(LoginWorker.KEY_USER_ID, userId)
                .putString(LoginWorker.KEY_USER_PASSWORD, passWord)
                .build()

        val work = OneTimeWorkRequestBuilder<LoginWorker>().setInputData(data).setConstraints(constraints).build()
        WorkManager.getInstance().enqueue(work)


        WorkManager.getInstance().getWorkInfoByIdLiveData(work.id)
                .observe(this, Observer { info ->
                    if (info != null && info.state.isFinished) {
                        val isSuccess = info.outputData.getBoolean(LoginWorker.KEY_RESULT, false)
                        if (isSuccess) {

                            val accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)

                            val loginData = Bundle()

                            val authToken = "xxx"
                            val tokenType = "xxx"

                            loginData.putString(AccountManager.KEY_ACCOUNT_NAME, userId)
                            loginData.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
                            loginData.putString(HormannAccountAuthenticator.TOKEN_TYPE, tokenType)
                            loginData.putString(AccountManager.KEY_AUTHTOKEN, authToken)
                            loginData.putString(HormannAccountAuthenticator.PASSWORD, passWord)

                            val result = Intent()
                            result.putExtras(loginData)

                            setLoginResult(result)

                            Toast.makeText(this@HormannAccountActivity, getString(R.string.logged_in), Toast.LENGTH_SHORT).show()
                        } else {
                            findViewById<ProgressBar>(R.id.login_progress).visibility = View.GONE
                            findViewById<ScrollView>(R.id.login_form).visibility = View.VISIBLE
                            Toast.makeText(this@HormannAccountActivity, getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show()
                        }


                    }
                })


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
}