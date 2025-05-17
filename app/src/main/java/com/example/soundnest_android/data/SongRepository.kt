package com.example.soundnest_android.data

import com.example.soundnest_android.business_logic.Comment
import com.example.soundnest_android.business_logic.Song

interface SongRepository {
    suspend fun getSongById(songId: Int): Song
    suspend fun getCommentsForSong(songId: Int): List<Comment>
}