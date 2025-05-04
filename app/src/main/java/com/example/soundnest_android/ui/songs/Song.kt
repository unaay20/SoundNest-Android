package com.example.soundnest_android.ui.songs

import java.io.Serializable

data class Song(
    val id: Long,             // ← nuevo
    val title: String,
    val artist: String,
    val coverResId: Int
) : Serializable