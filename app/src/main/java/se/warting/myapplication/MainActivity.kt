package se.warting.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.thomasletsch.Client
import de.thomasletsch.Discovery

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val server = object : doAsync() {
            override fun onPostExecute(result: Client?) {
                findViewById<TextView>(R.id.dummy).text = result.toString()
            }
        }

        server.execute()

        //val clientAPI = ClientAPI(client, "thomas", "aaabbbccc")
        //Log.d("LOGGER", "Name: " + clientAPI.getName())

    }
}

open class doAsync : AsyncTask<Void, Void, Client>() {
    override fun doInBackground(vararg params: Void?): Client {
        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = Client(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        return client
    }
}