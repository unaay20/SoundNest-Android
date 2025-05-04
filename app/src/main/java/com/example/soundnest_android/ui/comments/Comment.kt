package com.example.soundnest_android.ui.comments

import java.io.Serializable

data class Comment(
    val id: Int,            // o String, según tu backend
    val authorName: String,
    val text: String,
    val timestamp: Long      // opcional, para ordenar
) : Serializable