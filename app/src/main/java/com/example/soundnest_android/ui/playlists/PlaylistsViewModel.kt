package com.example.soundnest_android.ui.playlists

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.business_logic.Playlist
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.PlaylistService
import com.example.soundnest_android.restful.utils.ApiResult
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
                            .map { dto ->
                                val songs = dto.songs.map { rel ->
                                    Song(
                                        id = rel.songId,
                                        title = "",
                                        artist = "",
                                        coverUrl = null
                                    )
                                }
                                Playlist(
                                    id = dto.idPlaylist,
                                    name = dto.name,
                                    description = dto.description,
                                    songs = songs,
                                    imageUri = "$base${dto.pathImageUrl}"
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
                            id = it.idPlaylist,
                            name = it.name,
                            description = it.description,
                            songs = emptyList(), // al crear no traemos canciones
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
            val result = service.deletePlaylist(playlist.id)
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

    fun addSongToPlaylist(playlistId: String, songId: Int) {
        viewModelScope.launch {
            when (val result = service.addSongToPlaylist(songId.toString(), playlistId)) {
                is ApiResult.Success -> {
                    _playlists.value?.let { list ->
                        val idx = list.indexOfFirst { it.id == playlistId }
                        if (idx >= 0) {
                            val updated = list[idx].copy(
                                songs = list[idx].songs + Song(songId, /* title… */ "", "", null)
                            )
                            list[idx] = updated
                            _playlists.value = list
                        }
                    }
                }

                is ApiResult.HttpError -> _error.value = "HTTP error: ${result.message}"
                is ApiResult.NetworkError -> _error.value = "Error de red al añadir canción"
                is ApiResult.UnknownError -> _error.value = "Error desconocido al añadir canción"
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Int) {
        viewModelScope.launch {
            when (val result = service.removeSongFromPlaylist(songId.toString(), playlistId)) {
                is ApiResult.Success -> {
                    _playlists.value?.let { list ->
                        val idx = list.indexOfFirst { it.id == playlistId }
                        if (idx >= 0) {
                            val updated = list[idx].copy(
                                songs = list[idx].songs.filterNot { it.id == songId }
                            )
                            list[idx] = updated
                            _playlists.value = list
                        }
                    }
                }

                is ApiResult.HttpError -> _error.value = "HTTP error: ${result.message}"
                is ApiResult.NetworkError -> _error.value = "Error de red al eliminar canción"
                is ApiResult.UnknownError -> _error.value = "Error desconocido al eliminar canción"
            }
        }
    }
}
