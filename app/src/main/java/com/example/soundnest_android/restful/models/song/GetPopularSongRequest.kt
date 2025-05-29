package com.example.soundnest_android.restful.models.song

data class GetPopularSongRequest(
    val amount: Int,
    val year: Int,
    val month: Int
)