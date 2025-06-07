package com.example.soundnest_android.ui.upload_song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.services.SongService

class UploadSongViewModelFactory(
    private val grpcService: SongFileGrpcService,
    private val restService: SongService,
    private val tokenProvider: SharedPrefsTokenProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return UploadSongViewModel(grpcService, restService, tokenProvider) as T
    }
}