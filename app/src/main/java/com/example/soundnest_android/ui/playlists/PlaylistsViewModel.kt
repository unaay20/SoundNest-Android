package com.example.soundnest_android.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaylistsViewModel : ViewModel() {

    private val _playlists = MutableLiveData<MutableList<Playlist>>()
    val playlists: LiveData<MutableList<Playlist>> get() = _playlists

    init {
        val initialPlaylists = mutableListOf(
            Playlist("Rock Classics", listOf(), "img_party_background"),
            Playlist("Chill Vibes", listOf(), "img_party_background")
        )
        _playlists.value = initialPlaylists
    }

    fun createPlaylist(name: String, description: String, imageUri: String?) {
        val newPlaylist = Playlist(name, listOf(), imageUri ?: "img_party_background")
        _playlists.value?.add(newPlaylist)
        _playlists.value = _playlists.value
    }

    fun deletePlaylist(playlist: Playlist) {
        _playlists.value?.remove(playlist)
        _playlists.value = _playlists.value
    }
}
