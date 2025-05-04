package com.example.soundnest_android.data

import com.example.soundnest_android.ui.comments.Comment
import com.example.soundnest_android.ui.songs.Song

interface SongRepository {
    suspend fun getSongById(songId: Long): Song
    suspend fun getCommentsForSong(songId: Long): List<Comment>
}