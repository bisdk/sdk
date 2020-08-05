package org.bisdk.sdk

interface PackageListener {

    fun onPackageReceived(tc: TransportContainer)
}
