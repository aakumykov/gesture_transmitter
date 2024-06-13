package com.github.aakumykov.common.settings_provider

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.github.aakumykov.common.config.DEFAULT_SERVER_ADDRESS
import com.github.aakumykov.common.config.DEFAULT_SERVER_PATH
import com.github.aakumykov.common.config.DEFAULT_SERVER_PORT
import com.github.aakumykov.common.utils.NetworkAddressDetector

const val KEY_SERVER_ADDRESS = "SERVER_ADDRESS"
const val KEY_SERVER_PORT = "SERVER_PORT"
const val KEY_SERVER_PATH = "SERVER_PATH"


class SettingsProvider @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val networkAddressDetector: NetworkAddressDetector
) {

    fun storeIpAddress(value: String) {
        editor().putString(KEY_SERVER_ADDRESS, value).apply()
    }

    fun storePort(value: Int) {
        editor().putInt(KEY_SERVER_PORT, value).apply()
    }

    fun storePath(value: String) {
        editor().putString(KEY_SERVER_PATH, value).apply()
    }


    fun getIpAddress(): String {
        val storedIpAddress: String? = sharedPreferences.getString(KEY_SERVER_ADDRESS, null)
        val detectedIpAddress: String? = networkAddressDetector.ipAddressInLocalNetwork()
        val defaultIpAddress = DEFAULT_SERVER_ADDRESS

        return storedIpAddress ?: detectedIpAddress ?: defaultIpAddress
    }

    fun getPort(): Int
        = sharedPreferences.getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)

    fun getPath(): String
        = sharedPreferences.getString(KEY_SERVER_PATH, null) ?: DEFAULT_SERVER_PATH


    private fun editor(): SharedPreferences.Editor = sharedPreferences.edit()

}