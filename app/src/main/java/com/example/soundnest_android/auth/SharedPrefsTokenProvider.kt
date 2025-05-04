package com.example.soundnest_android.auth

import android.content.Context
import restful.utils.TokenProvider

class SharedPrefsTokenProvider(context: Context) : TokenProvider {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val KEY_TOKEN = "key_token"

    override fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    override fun shouldAttachToken(): Boolean =
        !getToken().isNullOrBlank()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }
}