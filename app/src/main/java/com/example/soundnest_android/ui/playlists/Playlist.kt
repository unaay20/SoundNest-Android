package com.example.soundnest_android.ui.playlists

data class Playlist(
    val name: String,
    val songCount: Int,
    val imageResId: Int // o String si cargas desde URL
)