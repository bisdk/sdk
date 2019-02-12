package com.hormann.app

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.hormann.app.account.HormannAccountAuthenticator
import de.thomasletsch.Client
import de.thomasletsch.ClientAPI
import kotlinx.android.synthetic.main.activity_nav.*
import kotlinx.android.synthetic.main.app_bar_nav.*
import kotlinx.android.synthetic.main.content_nav.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onResume() {
        super.onResume()
        val accountManager = AccountManager.get(this)
        val accounts = accountManager.getAccountsByType(HormannAccountAuthenticator.ACCOUNT_TYPE)
        if (accounts.isEmpty()) {
            buttonAddGateway.visibility = View.VISIBLE
        } else {
            buttonAddGateway.visibility = View.GONE
            setup(accounts[0].name, accountManager.getPassword(accounts[0]))
        }
    }

    @SuppressLint("SetTextI18n")
    fun setup(account: String, password: String) {
        val job = GlobalScope.launch(Dispatchers.IO) {

            updateUi("Welcome")
            updateUi("Setting up client")
            val client = Client(InetAddress.getByName(account.split("@")[1]), "000000000000", "1410ECD7ECD6")

            updateUi("Setting up client API")
            val clientAPI = ClientAPI(client, account.split("@")[0], password)
            updateUi("Name: ${clientAPI.getName()}")
            updateUi("Ping: ${clientAPI.ping()}")
            updateUi("Login in...")
            clientAPI.login()
            val state = clientAPI.getState()
            //updateUi("State: $state")
            val groups = clientAPI.getGroups()
            updateUi("Groups: $groups")
            val transition = clientAPI.getTransition(groups[0].ports[0])
            updateUi("Groups: $transition")
//        clientAPI.setState(groups[0].ports[0])
            updateUi("Logging out...")
            clientAPI.logout()
            updateUi("Logged out")
        }
    }

    private fun updateUi(s: String) {
        this@MainActivity.runOnUiThread {
            hello.text = "${hello.text}$s\n"
        }
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
