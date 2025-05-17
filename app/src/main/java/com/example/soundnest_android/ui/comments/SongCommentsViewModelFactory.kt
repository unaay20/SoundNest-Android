package com.example.soundnest_android.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.restful.utils.TokenProvider

class SongCommentsViewModelFactory(
    private val tokenProvider: TokenProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongCommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongCommentsViewModel(tokenProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
