package com.example.soundnest_android.restful.models.playlist

import com.squareup.moshi.Json

data class EditPlaylistRequest(
    @Json(name = "playlist_name") val playlistName: String,
    val description: String?
)