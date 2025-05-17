package com.example.soundnest_android.restful.models.comment

import com.squareup.moshi.Json

data class RespondCommentRequest(
    @Json(name = "message")
    val message: String
)