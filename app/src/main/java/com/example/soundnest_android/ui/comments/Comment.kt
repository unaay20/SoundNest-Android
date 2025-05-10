package com.example.soundnest_android.ui.comments

import java.io.Serializable

data class Comment(
    val song_id: Int,
    val user: String,
    val message: String,
) : Serializable