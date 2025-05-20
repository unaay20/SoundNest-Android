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

    fun playFromFile(song: Song, file: File) {
        _pendingFile.value = song to file
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}
