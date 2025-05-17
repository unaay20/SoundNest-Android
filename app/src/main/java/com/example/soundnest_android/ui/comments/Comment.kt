package com.example.soundnest_android.ui.comments

import java.io.Serializable

data class Comment(
    val id: String,
    val songId: Int,
    val user: String,
    val message: String,
    val parentId: String? = null,
    val timestamp: String? = null
) : Serializable
