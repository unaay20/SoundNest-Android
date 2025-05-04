package com.example.soundnest_android.ui.songs

import java.io.Serializable

data class Song(
    val title: String,
    val artist: String,
    val coverResId: Int  // o String si viene de URL
) : Serializable