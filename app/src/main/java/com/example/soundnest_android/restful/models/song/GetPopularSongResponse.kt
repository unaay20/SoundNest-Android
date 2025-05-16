package com.example.soundnest_android.restful.models.song

import com.squareup.moshi.Json

data class GetPopularSongResponse(
    @Json(name="idSong")           val idSong: Int,
    @Json(name="songName")         val songName: String?,
    @Json(name="fileName")         val fileName: String,
    @Json(name="durationSeconds")  val durationSeconds: Int,
    @Json(name="releaseDate")      val releaseDate: String,
    @Json(name="isDeleted")        val isDeleted: Boolean,
    @Json(name="idSongGenre")      val idSongGenre: Int,
    @Json(name="idSongExtension")  val idSongExtension: Int,
    @Json(name="userName")         val userName: String?,
    @Json(name="description")      val description: String?,
    @Json(name="pathImageUrl")     val pathImageUrl: String?,
    @Json(name="visualizations")   val visualizations: List<Visualization> = emptyList()
)