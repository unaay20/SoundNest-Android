package com.example.soundnest_android.business_logic

import java.io.Serializable

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val duration: Int,
    val releaseDate: String,
    val description: String?
) : Serializable