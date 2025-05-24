package com.example.soundnest_android.restful.models.playlist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongIdsRequest(
    @Json(name = "songIds")
    val songIds: List<Int>
)
