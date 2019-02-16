package com.hormann.app

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.thomasletsch.Client
import de.thomasletsch.ClientAPI
import de.thomasletsch.Group
import java.net.InetAddress

class NameViewModel : ViewModel() {

    val host: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val mac: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val token: MutableLiveData<String> by lazy {
        MutableLiveData<String>("00000000")
    }

    val client: MediatorLiveData<Client> by lazy {
        MediatorLiveData<Client>()
    }
    val clientApi: MediatorLiveData<ClientAPI> by lazy {
        MediatorLiveData<ClientAPI>()
    }

    val isConnected: MediatorLiveData<Boolean> by lazy {
        MediatorLiveData<Boolean>()
    }

    val name: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val ping: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val state: MutableLiveData<HashMap<String, Int>> by lazy {
        MutableLiveData<HashMap<String, Int>>()
    }
    val groups: MutableLiveData<List<Group>> by lazy {
        MutableLiveData<List<Group>>()
    }


    init {
        // here our MediatorLiveData is basically a proxy to dbBooks
        client.addSource(host) {
            client.value?.close()
            Coroutines.ioThenMain({
                getClient(it, mac.value, token.value)
            }) { client ->
                this.client.value = client
            }
        }
        client.addSource(mac) {
            client.value?.close()
            Coroutines.ioThenMain({
                getClient(host.value, it, token.value)
            }) { client ->
                this.client.value = client
            }
        }
        client.addSource(token) { token ->
            if (client.value == null) {
                Coroutines.ioThenMain({
                    getClient(host.value, mac.value, token)
                }) { client ->
                    this.client.value = client
                }
            } else {
                client.value?.setTokenOrDefault(token)
            }
        }
        clientApi.addSource(client) {
            clientApi.value?.client?.close()
            clientApi.value = getClientApi(it)
        }

        isConnected.addSource(clientApi) {
            isConnected.value = it != null
        }
    }


    private fun getClient(h: String?, m: String?, t: String?): Client? {
        if (h != null && m != null && t != null) {
            return Client(InetAddress.getByName(h), getMacAddress(), m, t)
        }
        return null
    }

    private fun getClientApi(c: Client?): ClientAPI? {
        if (c != null) {
            return ClientAPI(c)
        }
        return null
    }

    fun requestName() {
        Coroutines.ioThenMain({
            clientApi.value?.getName()
        }) {
            this.name.value = it
        }
    }

    fun requestPing() {
        Coroutines.ioThenMain({
            val startTimeInMillis = System.currentTimeMillis()
            clientApi.value?.ping()
            "Got response after " + (System.currentTimeMillis() - startTimeInMillis) + " milliseconds"
        }) {
            this.ping.value = it
        }
    }

    fun requestState() {
        Coroutines.ioThenMain({
            clientApi.value?.getState()
        }) {
            this.state.value = it
        }
    }

    fun requestGroups() {
        Coroutines.ioThenMain({
            clientApi.value?.getGroups()
        }) {
            this.groups.value = it
        }
    }

    fun logout() {
        Coroutines.ioThenMain({
            clientApi.value?.logout()
        }) {
            this.token.value = "00000000"
        }
    }

}