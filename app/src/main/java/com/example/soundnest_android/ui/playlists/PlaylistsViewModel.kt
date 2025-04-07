package com.example.soundnest_android.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaylistsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is playlists Fragment"
    }
    val text: LiveData<String> = _text
}