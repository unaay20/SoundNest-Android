package com.example.soundnest_android.auth

import android.content.Context
import com.example.soundnest_android.restful.utils.TokenProvider

class SharedPrefsTokenProvider(context: Context) : TokenProvider {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val KEY_TOKEN = "key_token"
    private val KEY_USERNAME = "key_username"

    override fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    override fun shouldAttachToken(): Boolean =
        !getToken().isNullOrBlank()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? =
        prefs.getString(KEY_USERNAME, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
