// PlaylistsViewModelFactory.kt
package com.example.soundnest_android.ui.playlists

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.restful.services.PlaylistService
import com.example.soundnest_android.restful.utils.TokenProvider

class PlaylistsViewModelFactory(
    private val application: Application,
    private val baseUrl: String,
    private val tokenProvider: TokenProvider,
    private val userId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistsViewModel(
                application = application,
                service = PlaylistService(baseUrl, tokenProvider),
                userId = userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
