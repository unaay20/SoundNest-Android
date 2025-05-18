package com.example.soundnest_android.restful.models.playlist

import com.squareup.moshi.Json

data class CreatePlaylistResponse(
    @Json(name = "idPlaylist") val idPlaylist: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "pathImageUrl") val pathImageUrl: String?,
    @Json(name = "userId") val userId: Int,
    @Json(name = "createdAt") val createdAt: String
)