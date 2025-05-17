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
        Comment(song_id = 1, user = "User1", message = "¡Me encanta esta canción!"),
        Comment(song_id = 2, user = "User2", message = "El ritmo es brutal.")
    )

    private val commentsBySong = mapOf(
        1 to comments.subList(0, 1),
        2 to comments.subList(0, 2)
    )

    override suspend fun getSongById(songId: Int): Song =
        songs.first { it.id == songId }

    override suspend fun getCommentsForSong(songId: Int): List<Comment> =
        commentsBySong[songId] ?: emptyList()
}
