package com.example.soundnest_android.business_logic

data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val songs: List<Song>,
    val imageUri: String?
)