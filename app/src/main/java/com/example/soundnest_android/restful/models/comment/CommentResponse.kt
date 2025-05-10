package com.example.soundnest_android.restful.models.comment

import com.squareup.moshi.Json

data class CommentResponse(
    @Json(name = "_id")
    val id: String,

    @Json(name = "song_id")
    val songId: Int,

    @Json(name = "user")
    val user: String,

    @Json(name = "message")
    val message: String,

    @Json(name = "parent_id")
    val parentId: String?,

    @Json(name = "timestamp")
    val timestamp: String?,

    @Json(name = "responses")
    val responses: List<CommentResponse>? = emptyList(),

    @Json(name = "__v")
    val v: Int? = null
)
