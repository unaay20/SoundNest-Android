package com.example.soundnest_android.data

import com.example.soundnest_android.R
import com.example.soundnest_android.ui.comments.Comment
import com.example.soundnest_android.ui.songs.Song

class InMemorySongRepository : SongRepository {

    private val songs = listOf(
        Song(id = 1, title = "Song A", artist = "Artist 1", coverResId = R.drawable.im_cover_bohemian),
        Song(id = 2, title = "Song B", artist = "Artist 2", coverResId = R.drawable.img_cover_imagine),
    )

    private val comments = listOf(
        Comment(
            id = 1,
            authorName = "User1",
            text = "¡Me encanta esta canción!",
            timestamp = 1683004800000L  // e.g. Mon May  2 00:00:00 GMT-06:00 2025
        ),
        Comment(
            id = 2,
            authorName = "User2",
            text = "El ritmo es brutal.",
            timestamp = 1683008400000L  // e.g. Mon May  2 01:00:00 GMT-06:00 2025
        )
    )
    private val commentsBySong = mapOf(
        1L to comments.subList(0,1),
        2L to comments.subList(0,2)
    )

    override suspend fun getSongById(songId: Long): Song {
        // Aquí podrías lanzar excepción si no existe
        return songs.first { it.id == songId }
    }

    override suspend fun getCommentsForSong(songId: Long): List<Comment> {
        return commentsBySong[songId] ?: emptyList()
    }
}
