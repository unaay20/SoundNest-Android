package com.example.soundnest_android.restful.models.playlist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongInPlaylistResponse(
    @Json(name = "song_id")   val songId: Int,
    @Json(name = "addedAt")  val addedAt: String,
    @Json(name = "_id")       val id: String
)
