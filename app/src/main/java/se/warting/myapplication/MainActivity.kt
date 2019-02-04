package se.warting.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thomasletsch.Client
import de.thomasletsch.ClientAPI
import de.thomasletsch.Discovery

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val discovery = Discovery()
        val future = discovery.startServer()
        discovery.sendDiscoveryRequest()
        val discoveryData = future.join()

        val client = Client(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())
        val clientAPI = ClientAPI(client, "thomas", "aaabbbccc")
        println("Name: " + clientAPI.getName())
    }
}
