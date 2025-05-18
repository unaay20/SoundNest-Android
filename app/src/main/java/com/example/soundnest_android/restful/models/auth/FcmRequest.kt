package com.example.soundnest_android.restful.models.auth

import com.squareup.moshi.Json

data class FcmTokenRequest(
    @Json(name = "token")
    val token: String,

    @Json(name = "device")
    val device: String? = null,

    @Json(name = "platform_version")
    val platformVersion: String? = null,

    @Json(name = "app_version")
    val appVersion: String? = null
)