package com.example.soundnest_android.restful.models.user

import com.squareup.moshi.Json

data class EditUserPasswordRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "code")
    val code: String,

    @Json(name = "newPassword")
    val newPassword: String,
)