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
            song_id = 1,
            user = "User1",
            message = "¡Me encanta esta canción!"
        ),
        Comment(
            song_id = 2,
            user = "User2",
            message = "El ritmo es brutal."
        )
    )
    private val commentsBySong = mapOf(
        1 to comments.subList(0,1),
        2 to comments.subList(0,2)
    )

    override suspend fun getSongById(songId: Int): Song {
        return songs.first { it.id == songId }
    }

    override suspend fun getCommentsForSong(songId: Int): List<Comment> {
        return commentsBySong[songId] ?: emptyList()
    }
}
