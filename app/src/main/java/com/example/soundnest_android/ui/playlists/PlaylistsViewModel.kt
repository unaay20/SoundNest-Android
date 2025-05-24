package com.example.soundnest_android.ui.playlists

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.soundnest_android.business_logic.Playlist
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.services.PlaylistService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlaylistsViewModel(
    application: Application,
    private val service: PlaylistService,
    private val userId: String
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PlaylistsViewModel"
    }

    private val _playlists = MutableLiveData<MutableList<Playlist>>(mutableListOf())
    val playlists: LiveData<MutableList<Playlist>> = _playlists

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = service.fetchByUser(userId)
            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        val base = RestfulRoutes.getBaseUrl().removeSuffix("/")
                        _playlists.value = result.data?.playlists.orEmpty()
                            .map {
                                Playlist(
                                    id       = it.idPlaylist,
                                    name     = it.name,
                                    description = it.description,
                                    songs    = emptyList(),
                                    imageUri = "$base${it.pathImageUrl}"
                                )
                            }
                            .toMutableList()
                    }
                    is ApiResult.HttpError -> {
                        Log.e(TAG, "HTTP error: ${result.message}")
                        _error.value = "Error al cargar playlists: ${result.message}"
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "Network error", result.exception)
                        _error.value = "Problema de red al cargar playlists"
                    }
                    is ApiResult.UnknownError -> {
                        Log.e(TAG, "Unknown error", result.exception)
                        _error.value = "Error desconocido al cargar playlists"
                    }
                }
            }
        }
    }

    fun createPlaylist(name: String, description: String, imageFile: File?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (imageFile == null) {
                withContext(Dispatchers.Main) {
                    _error.value = "Selecciona una imagen para la playlist"
                }
                return@launch
            }
            val result = service.createPlaylist(name, description, imageFile)
            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> result.data?.let {
                        val base = RestfulRoutes.getBaseUrl().removeSuffix("/")
                        val nueva = Playlist(
                            id       = it.idPlaylist,
                            name     = it.name,
                            description = it.description,
                            songs    = emptyList(),
                            imageUri = "$base/uploads/${it.pathImageUrl}"
                        )
                        _playlists.value?.apply {
                            add(nueva)
                            _playlists.value = this
                        }
                    }
                    is ApiResult.HttpError -> {
                        Log.e(TAG, "Error HTTP: ${result.message}")
                        _error.value = "Error HTTP: ${result.message}"
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "Error de red", result.exception)
                        _error.value = "Error de red"
                    }
                    is ApiResult.UnknownError -> {
                        Log.e(TAG, "Error desconocido", result.exception)
                        _error.value = "Error desconocido"
                    }
                }
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = service.deletePlaylist(playlist.id.toString())
            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> _playlists.value?.apply {
                        remove(playlist)
                        _playlists.value = this
                    }
                    is ApiResult.HttpError -> {
                        Log.e(TAG, "HTTP error deleting playlist: ${result.message}")
                        _error.value = "No se pudo borrar playlist: ${result.message}"
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "Network error deleting playlist", result.exception)
                        _error.value = "Problema de red al borrar playlist"
                    }
                    is ApiResult.UnknownError -> {
                        Log.e(TAG, "Unknown error deleting playlist", result.exception)
                        _error.value = "Error desconocido al borrar playlist"
                    }
                }
            }
        }
    }
}
