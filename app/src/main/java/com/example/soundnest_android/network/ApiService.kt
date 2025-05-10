package com.example.soundnest_android.network

import android.content.Context
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.services.BaseService
import com.example.soundnest_android.restful.utils.TokenProvider

object ApiService : BaseService("http://192.168.100.42/restful/") {
    lateinit var tokenProvider: SharedPrefsTokenProvider
        private set

    fun init(context: Context) {
        tokenProvider = SharedPrefsTokenProvider(context)
    }
}
