package com.github.aakumykov.settings_provider

import android.content.SharedPreferences
import com.github.aakumykov.common.config.DEFAULT_SERVER_ADDRESS
import com.github.aakumykov.common.config.DEFAULT_SERVER_PATH
import com.github.aakumykov.common.config.DEFAULT_SERVER_PORT
import javax.inject.Inject

const val KEY_SERVER_ADDRESS = "SERVER_ADDRESS"
const val KEY_SERVER_PORT = "SERVER_PORT"
const val KEY_SERVER_PATH = "SERVER_PATH"


class SettingsProvider @Inject constructor(
    private val sharedPreferences: SharedPreferences
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


    fun getIpAddress(): String
        = sharedPreferences.getString(KEY_SERVER_ADDRESS, null) ?: DEFAULT_SERVER_ADDRESS

    fun getPort(): Int
        = sharedPreferences.getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)

    fun getPath(): String
        = sharedPreferences.getString(KEY_SERVER_PATH, null) ?: DEFAULT_SERVER_PATH


    private fun editor(): SharedPreferences.Editor = sharedPreferences.edit()

}