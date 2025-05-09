package com.example.soundnest_android.ui.playlists

import com.example.soundnest_android.ui.songs.Song

data class Playlist(
    val name: String,
    val songs: List<Song>,
    val imageUri: String?
)