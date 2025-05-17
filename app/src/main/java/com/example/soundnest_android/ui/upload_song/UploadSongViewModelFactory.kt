package com.example.soundnest_android.ui.upload_song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.grpc.services.SongFileGrpcService

class UploadSongViewModelFactory(
    private val grpcService: SongFileGrpcService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadSongViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadSongViewModel(grpcService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
