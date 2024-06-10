package com.github.aakumykov.client.settings_provider

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

const val KEY_SERVER_ADDRESS = "SERVER_ADDRESS"
const val KEY_SERVER_PORT = "SERVER_PORT"
const val KEY_SERVER_PATH = "SERVER_PATH"

const val DEFAULT_SERVER_ADDRESS = "192.168.0.119"
const val DEFAULT_SERVER_PORT = 8081
const val DEFAULT_SERVER_PATH = "gestures"


class SettingsProvider private constructor(private val applicationContext: Context) {

    fun storeIpAddress(value: String) {
        editor().putString(KEY_SERVER_ADDRESS, value).apply()
    }

    fun storePort(value: Int) {
        editor().putInt(KEY_SERVER_PORT, value).apply()
    }

    fun storePath(value: String) {
        editor().putString(KEY_SERVER_PATH, value).apply()
    }


    fun getIpAddress(): String?
        = sharedPreferences.getString(KEY_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS)

    fun getPort(): Int
        = sharedPreferences.getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)

    fun getPath(): String?
        = sharedPreferences.getString(KEY_SERVER_PATH, DEFAULT_SERVER_PATH)


    private fun editor(): SharedPreferences.Editor = sharedPreferences.edit()


    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }


    companion object {
        private var _ouwInstance: SettingsProvider? = null
        @JvmStatic
        fun getInstance(applicationContext: Context): SettingsProvider {
            if (null == _ouwInstance)
                _ouwInstance = SettingsProvider(applicationContext)
            return _ouwInstance!!
        }
    }
}