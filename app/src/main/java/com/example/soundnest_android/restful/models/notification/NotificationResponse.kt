package com.example.soundnest_android.restful.models.notification

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class NotificationResponse(
    @Json(name = "_id")
    val id: String? = null,

    @Json(name = "title")
    val title: String? = null,

    @Json(name = "sender")
    val sender: String? = null,

    @Json(name = "user_id")
    val userId: Int? = null,

    @Json(name = "user")
    val user: String? = null,

    @Json(name = "notification")
    val notification: String? = null,

    @Json(name = "relevance")
    val relevance: String? = null,

    @Json(name = "read")
    val read: Boolean? = null,

    @Json(name = "createdAt")
    val createdAt: String? = null,

    @Json(name = "__v")
    val version: Int? = null
)

@JsonClass(generateAdapter = false)
enum class Relevance {
    @Json(name = "low")
    low,

    @Json(name = "medium")
    medium,

    @Json(name = "high")
    high
}