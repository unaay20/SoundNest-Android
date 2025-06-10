package com.example.soundnest_android.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soundnest_android.business_logic.Song
import java.io.File

class SharedPlayerViewModel : ViewModel() {
    private val _pendingFile = MutableLiveData<Pair<Song, File>>()
    val pendingFile: LiveData<Pair<Song, File>> = _pendingFile
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _playlist = MutableLiveData<List<Song>?>(null)
    val playlist: LiveData<List<Song>?> = _playlist
    private val _currentIndex = MutableLiveData<Int>(-1)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _isPlayingLive = MutableLiveData(false)
    val isPlayingLive: LiveData<Boolean> = _isPlayingLive

    fun setIsPlaying(playing: Boolean) {
        _isPlayingLive.value = playing
    }

    fun playFromFile(song: Song, file: File) {
        _pendingFile.value = song to file
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setPlaylist(songs: List<Song>) {
        _playlist.value = songs
        _currentIndex.value = 0
    }

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun clearPlaylist() {
        _playlist.value = null
        _currentIndex.value = -1
    }
}
