package com.example.soundnest_android

import android.app.Application
import com.example.soundnest_android.network.ApiClient

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}