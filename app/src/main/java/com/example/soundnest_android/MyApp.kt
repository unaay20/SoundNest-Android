package com.example.soundnest_android

import android.app.Application
import com.example.soundnest_android.network.ApiService

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiService.init(this)
    }
}