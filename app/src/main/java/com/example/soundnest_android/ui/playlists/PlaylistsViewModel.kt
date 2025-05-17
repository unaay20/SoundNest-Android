package com.example.soundnest_android.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soundnest_android.R
import com.example.soundnest_android.ui.songs.Song

class PlaylistsViewModel : ViewModel() {

    private val _playlists = MutableLiveData<MutableList<Playlist>>()
    val playlists: LiveData<MutableList<Playlist>> get() = _playlists

    init {
        val initialPlaylists = mutableListOf(
            Playlist("Rock Classics", mutableListOf(
                Song(1, "Bohemian Rhapsody", "Queen", "https://upload.wikimedia.org/wikipedia/en/2/20/ImagineCover.jpg"),
                Song(123, "Stairway to Heaven", "Led Zeppelin", "https://upload.wikimedia.org/wikipedia/en/2/20/ImagineCover.jpg"),
                Song(123, "Hotel California", "Eagles", "https://upload.wikimedia.org/wikipedia/en/2/20/ImagineCover.jpg")
            ), "img_party_background"),
            Playlist("Chill Vibes", mutableListOf(), "img_party_background")
        )
        _playlists.value = initialPlaylists
    }

    fun createPlaylist(name: String, description: String, imageUri: String?) {
        val newPlaylist = Playlist(name, mutableListOf(), imageUri ?: "img_party_background")
        _playlists.value?.add(newPlaylist)
        _playlists.value = _playlists.value
    }

    fun deletePlaylist(playlist: Playlist) {
        _playlists.value?.remove(playlist)
        _playlists.value = _playlists.value
    }
}
