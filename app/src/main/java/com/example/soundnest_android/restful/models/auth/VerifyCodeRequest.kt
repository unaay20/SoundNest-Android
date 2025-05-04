package com.example.soundnest_android.restful.models.auth

import com.squareup.moshi.Json

data class VerifyCodeRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "code")
    val code: Int
)