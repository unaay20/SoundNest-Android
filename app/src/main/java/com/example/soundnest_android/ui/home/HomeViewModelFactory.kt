package com.example.soundnest_android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.TokenProvider

class HomeViewModelFactory(
    private val songService: SongService,
    private val tokenProvider: TokenProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(songService, tokenProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
