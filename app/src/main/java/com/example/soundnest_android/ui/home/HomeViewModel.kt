package com.example.soundnest_android.ui.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.restful.models.song.GetPopularSongResponse
import com.example.soundnest_android.restful.models.song.GetRecentSongResponse
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import com.example.soundnest_android.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel (private val songService: SongService,
                     private val tokenProvider: TokenProvider) : ViewModel() {
    private val _navigateToNotifications = MutableLiveData<Boolean>()
    val navigateToNotifications: LiveData<Boolean> = _navigateToNotifications

    private val _popular = MutableLiveData<List<GetPopularSongResponse>>()
    val popular: MutableLiveData<List<GetPopularSongResponse>> = _popular

    private val _recent = MutableLiveData<List<GetRecentSongResponse>>()
    val recent: MutableLiveData<List<GetRecentSongResponse>> = _recent

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    fun onNotificationsClicked() {
        _navigateToNotifications.value = true
    }

    fun onNavigated() {
        _navigateToNotifications.value = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadSongs() {
        viewModelScope.launch {
            val now = LocalDate.now()

            when (val r = songService.getPopularByMonth(5, now.year, now.monthValue)) {
                is ApiResult.Success<List<GetPopularSongResponse>?> -> {
                    val list = r.data.orEmpty()
                    Log.d("HomeViewModel", "POPULARES ▶ servidor devolvió: $list")
                    _popular.value = list
                }
                is ApiResult.HttpError -> {
                    Log.e("HomeViewModel", "POPULARES HTTP ${r.code}: ${r.message}")
                    _error.value = "HTTP ${r.code}: ${r.message}"
                }
                is ApiResult.NetworkError -> {
                    Log.e("HomeViewModel", "POPULARES RED: ${r.exception}")
                    _error.value = "Red: ${r.exception.message}"
                }
                is ApiResult.UnknownError -> {
                    Log.e("HomeViewModel", "POPULARES ERROR: ${r.exception}")
                    _error.value = "Error: ${r.exception.message}"
                }
            }

            when (val r = songService.getRecent(10)) {
                is ApiResult.Success<List<GetRecentSongResponse>?> -> {
                    val list = r.data.orEmpty()
                    Log.d("HomeViewModel", "RECIENTES ▶ servidor devolvió: $list")
                    _recent.value = list
                }
                is ApiResult.HttpError -> {
                    Log.e("HomeViewModel", "RECIENTES HTTP ${r.code}: ${r.message}")
                    _error.value = "HTTP ${r.code}: ${r.message}"
                }
                is ApiResult.NetworkError -> {
                    Log.e("HomeViewModel", "RECIENTES RED: ${r.exception}")
                    _error.value = "Red: ${r.exception.message}"
                }
                is ApiResult.UnknownError -> {
                    Log.e("HomeViewModel", "RECIENTES ERROR: ${r.exception}")
                    _error.value = "Error: ${r.exception.message}"
                }
            }
        }
    }



    private val _navigateToUploadSong = MutableLiveData<Boolean>()
    val navigateToUploadSong: LiveData<Boolean> = _navigateToUploadSong

    fun onAddSongClicked() {
        _navigateToUploadSong.value = true
    }

    fun onAddSongNavigated() {
        _navigateToUploadSong.value = false
    }
}
