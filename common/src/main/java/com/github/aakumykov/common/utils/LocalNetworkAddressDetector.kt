package com.github.aakumykov.common.utils

import com.github.aakumykov.common.config.DEFAULT_SERVER_ADDRESS
import java.net.NetworkInterface

interface NetworkAddressDetector {
    fun ipAddressInLocalNetwork(): String?
}

object FakeNetworkAddressDetector : NetworkAddressDetector {
    override fun ipAddressInLocalNetwork(): String = DEFAULT_SERVER_ADDRESS
}

object LocalNetworkAddressDetector : NetworkAddressDetector {

    override fun ipAddressInLocalNetwork(): String? {
        return NetworkInterface.getNetworkInterfaces()
            .iterator()
            .asSequence()
            .flatMap {
                it.inetAddresses.asSequence()
                    .filter { inetAddress ->
                        val isLocalSiteAddress = inetAddress.isSiteLocalAddress
                        val notIpV6Address = !(inetAddress.hostAddress?.contains(":") ?: false)
                        val notLocalhostAddress = inetAddress.hostAddress != "127.0.0.1"
                        isLocalSiteAddress && notIpV6Address && notLocalhostAddress
                    }
                    .map { inetAddress -> inetAddress.hostAddress }
            }
            .firstOrNull()
    }
}