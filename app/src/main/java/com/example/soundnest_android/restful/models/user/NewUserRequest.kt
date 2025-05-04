package com.example.soundnest_android.restful.models.user

import com.squareup.moshi.Json

data class NewUserRequest(
    @Json(name = "nameUser")
    val nameUser: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "code")
    val code: String,

    @Json(name = "additionalInformation")
    val additionalInformation: AdditionalInformation
)

data class AdditionalInformation(
    @Json(name = "info")
    val info: List<String>
)