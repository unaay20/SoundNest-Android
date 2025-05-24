package com.example.soundnest_android.restful.models.playlist

import com.example.soundnest_android.business_logic.Song
import com.squareup.moshi.Json

data class PlaylistResponse(
    @Json(name = "_id")            val idPlaylist: String,
    @Json(name = "creator_id")     val creatorId: Int,
    @Json(name = "playlist_name")  val name: String,
    @Json(name = "description")    val description: String?,
    @Json(name = "image_path")     val pathImageUrl: String,
    @Json(name = "songs")          val songs: List<SongInPlaylistResponse>,
    @Json(name = "createdAt")      val createdAt: String
)
