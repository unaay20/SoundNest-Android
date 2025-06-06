package com.example.soundnest_android.restful.models.notification

import com.squareup.moshi.Json

data class CreateNotificationRequest(
    @Json(name = "title")
    val title: String,

    @Json(name = "sender")
    val sender: String,

    @Json(name = "user_id")
    val userId: Int,

    @Json(name = "user")
    val user: String,

    @Json(name = "notification")
    val notification: String,

    @Json(name = "relevance")
    val relevance: Relevance
)
