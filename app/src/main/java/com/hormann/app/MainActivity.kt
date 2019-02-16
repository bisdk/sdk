package com.hormann.app

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.hormann.app.account.HormannAccountAuthenticator
import kotlinx.android.synthetic.main.activity_nav.*
import kotlinx.android.synthetic.main.app_bar_nav.*
import kotlinx.android.synthetic.main.content_nav.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var model: NameViewModel


    private lateinit var accountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        setSupportActionBar(toolbar)

        buttonAddGateway.setOnClickListener { view ->
            AccountManager.get(view.context).addAccount(
                    HormannAccountAuthenticator.ACCOUNT_TYPE,
                    HormannAccountAuthenticator.TOKEN_TYPE_GATEWAY,
                    null,
                    null,
                    this,
                    null,
                    null
            )
        }

        accountManager = AccountManager.get(this)


        model = ViewModelProviders.of(this).get(NameViewModel::class.java)

        model.isConnected.observe(this, Observer { isConnected ->
            Toast.makeText(this@MainActivity, "IsConnected: $isConnected", Toast.LENGTH_SHORT).show()
        })
        model.name.observe(this, Observer {
            Toast.makeText(this@MainActivity, "Name: $it", Toast.LENGTH_SHORT).show()
        })
        model.ping.observe(this, Observer {
            Toast.makeText(this@MainActivity, "Ping: $it", Toast.LENGTH_SHORT).show()
        })
        model.state.observe(this, Observer {
            Toast.makeText(this@MainActivity, "State: $it", Toast.LENGTH_LONG).show()
        })
        model.groups.observe(this, Observer {
            Toast.makeText(this@MainActivity, "Groups: $it", Toast.LENGTH_LONG).show()
        })

        buttonGetName.setOnClickListener { model.requestName() }
        buttonPing.setOnClickListener { model.requestPing() }
        buttonState.setOnClickListener { model.requestState() }
        buttonGroups.setOnClickListener { model.requestGroups() }
        buttonLogout.setOnClickListener {
            accountManager.invalidateAuthToken(HormannAccountAuthenticator.TOKEN_TYPE_GATEWAY, model.token.value)
            model.logout()
            accountManager.removeAccount(currentAccount, this@MainActivity, null, null)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        accountManager.addOnAccountsUpdatedListener(listener, null, true)

    }

    override fun onDestroy() {
        super.onDestroy()
        accountManager.removeOnAccountsUpdatedListener(listener)
    }

    private var currentAccount: Account? = null


    val listener = OnAccountsUpdateListener {
        it.iterator().forEach { account ->
            if (account.type == HormannAccountAuthenticator.ACCOUNT_TYPE) {
                setAccount(account)
                return@OnAccountsUpdateListener
            }
        }
        removeAccount()
    }


    private fun setAccount(account: Account) {
        currentAccount = account
        buttonsContainer.visibility = View.VISIBLE
        buttonAddGateway.visibility = View.GONE
        loadToken(accountManager, account)
    }

    private fun removeAccount() {
        buttonAddGateway.visibility = View.VISIBLE
        buttonsContainer.visibility = View.GONE
    }


    private fun loadToken(accountManager: AccountManager, account: Account) {
        accountManager.getAuthToken(account, HormannAccountAuthenticator.TOKEN_TYPE, null, this, {
            val token: String? = it.result.getString(AccountManager.KEY_AUTHTOKEN)
            if (token != null) {
                model.host.value = accountManager.getUserData(account, HormannAccountAuthenticator.KEY_USER_DATA_HOST)
                model.mac.value = accountManager.getUserData(account, HormannAccountAuthenticator.KEY_USER_DATA_MAC)
                model.token.value = token
            }
        }, null)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.nav, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
