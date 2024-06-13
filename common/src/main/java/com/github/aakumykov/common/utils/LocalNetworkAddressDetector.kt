package com.github.aakumykov.common.utils

import java.net.NetworkInterface
import javax.inject.Inject

class NetworkAddressDetector @Inject constructor() {

    fun ipAddressInLocalNetwork(): String? {
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