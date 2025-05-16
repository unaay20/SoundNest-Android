package com.example.soundnest_android.restful.models.song

import com.squareup.moshi.Json

data class Visualization(
    @Json(name="idVisualizations") val idVisualizations: Int,
    @Json(name="playCount")        val playCount: Int,
    @Json(name="period")           val period: String,
    @Json(name="idSong")           val idSong: Int
)