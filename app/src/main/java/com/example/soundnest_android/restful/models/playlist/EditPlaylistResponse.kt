package com.example.soundnest_android.restful.models.playlist

import com.squareup.moshi.Json

data class EditPlaylistResponse(
    @Json(name = "_id") val idPlaylist: String,
    @Json(name = "creator_id") val creatorId: Int,
    @Json(name = "playlist_name") val name: String,
    val description: String?,
    @Json(name = "image_path") val pathImageUrl: String?,
    val songs: List<Any>,
    val createdAt: String,
    @Json(name = "__v") val version: Int? = null
)