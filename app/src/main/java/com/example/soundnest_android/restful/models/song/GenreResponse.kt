package com.example.soundnest_android.restful.models.song

import com.squareup.moshi.Json

data class GenreResponse(
    @Json(name = "idSongGenre") val idSongGenre: Int,
    @Json(name = "genreName") val genreName: String
)