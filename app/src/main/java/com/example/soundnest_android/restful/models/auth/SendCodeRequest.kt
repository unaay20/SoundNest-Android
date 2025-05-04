package com.example.soundnest_android.restful.models.auth

import com.squareup.moshi.Json

data class SendCodeRequest(
    @Json(name = "email")
    val email: String
)