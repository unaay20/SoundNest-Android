package com.example.soundnest_android.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soundnest_android.business_logic.Song

class SharedPlayerViewModel : ViewModel() {
    private val _pending   = MutableLiveData<Pair<Song, ByteArray>>()
    val pending: LiveData<Pair<Song, ByteArray>> = _pending

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun queue(song: Song, data: ByteArray) {
        _pending.value = song to data
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}

