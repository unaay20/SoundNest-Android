package com.example.soundnest_android.business_logic

data class Playlist(
    val name: String,
    val songs: List<Song>,
    val imageUri: String?
)