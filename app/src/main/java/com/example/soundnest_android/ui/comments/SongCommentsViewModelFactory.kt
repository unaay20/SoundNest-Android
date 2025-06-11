package com.example.soundnest_android.ui.comments

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.restful.utils.TokenProvider

class SongCommentsViewModelFactory(
    private val application: Application,
    private val tokenProvider: TokenProvider
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SongCommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            SongCommentsViewModel(application, tokenProvider) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
