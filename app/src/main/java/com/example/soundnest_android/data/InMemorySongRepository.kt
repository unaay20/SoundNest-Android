package com.example.soundnest_android.data

import com.example.soundnest_android.ui.comments.Comment
import com.example.soundnest_android.ui.songs.Song

class InMemorySongRepository : SongRepository {

    private val songs = listOf(
        Song(
            id       = 1,
            title    = "Bohemian Rhapsody",
            artist   = "Queen",
            coverUrl = "https://upload.wikimedia.org/wikipedia/en/9/9f/Bohemian_Rhapsody.png"
        ),
        Song(
            id       = 2,
            title    = "Imagine",
            artist   = "John Lennon",
            coverUrl = "https://upload.wikimedia.org/wikipedia/en/2/20/ImagineCover.jpg"
        )
    )

    private val comments = listOf(
        Comment(
            id        = "c1",
            songId    = 1,
            user      = "User1",
            message   = "¡Me encanta esta canción!",
            parentId  = null,
            timestamp = "2025-05-17T10:00:00Z"
        ),
        Comment(
            id        = "c2",
            songId    = 2,
            user      = "User2",
            message   = "El ritmo es brutal.",
            parentId  = null,
            timestamp = "2025-05-17T10:05:00Z"
        )
    )

    // Si quieres añadir alguna respuesta anidada de ejemplo:
    private val commentsWithReplies = listOf(
        Comment(
            id        = "c3",
            songId    = 1,
            user      = "User3",
            message   = "Totalmente de acuerdo",
            parentId  = "c1",
            timestamp = "2025-05-17T10:15:00Z"
        )
    )

    private val commentsBySong = mapOf(
        1 to (comments.filter { it.songId == 1 } + commentsWithReplies),
        2 to comments.filter { it.songId == 2 }
    )

    override suspend fun getSongById(songId: Int): Song =
        songs.first { it.id == songId }

    override suspend fun getCommentsForSong(songId: Int): List<Comment> =
        commentsBySong[songId] ?: emptyList()
}
