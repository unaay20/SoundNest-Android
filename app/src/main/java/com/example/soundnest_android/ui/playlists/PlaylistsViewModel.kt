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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLConnection

class PlaylistsViewModel(
    application: Application,
    private val service: PlaylistService,
    private val userId: String
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PlaylistsViewModel"
    }

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

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
                                    imageUri = dto.pathImageUrl?.let { "$base$it" }
                                )
                            }
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

            val mimeType = getMimeType(imageFile) ?: "image/jpeg" // Default MIME type
            val requestBody = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
            val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

            val result = service.createPlaylist(namePart, descPart, imagePart)


            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        loadPlaylists()
                    }

                    is ApiResult.HttpError -> {
                        _error.value = "Error HTTP: ${result.message}"
                    }

                    is ApiResult.NetworkError -> {
                        _error.value = "Error de red"
                    }

                    is ApiResult.UnknownError -> {
                        _error.value = "Error desconocido"
                    }
                }
            }
        }
    }

    private fun getMimeType(file: File): String? {
        return URLConnection.guessContentTypeFromName(file.name)
    }


    fun deletePlaylist(playlistToDelete: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = service.deletePlaylist(playlistToDelete.id)
            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        val currentList = _playlists.value.orEmpty()
                        _playlists.value = currentList.filterNot { it.id == playlistToDelete.id }
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
                    val currentPlaylists = _playlists.value.orEmpty()
                    val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlistId }

                    if (playlistIndex != -1) {
                        val targetPlaylist = currentPlaylists[playlistIndex]
                        val newSong =
                            Song(songId, /* title… */ "Unknown Title", "Unknown Artist", null)

                        val updatedPlaylist = targetPlaylist.copy(
                            songs = targetPlaylist.songs + newSong
                        )

                        val newPlaylists = currentPlaylists.toMutableList().apply {
                            this[playlistIndex] = updatedPlaylist
                        }.toList()

                        _playlists.value = newPlaylists
                        loadPlaylists()
                    } else {
                        Log.w(TAG, "addSongToPlaylist: Playlist with ID $playlistId not found.")
                    }
                }

                is ApiResult.HttpError -> {
                    Log.e(TAG, "addSongToPlaylist HTTP error: ${result.message}")
                    _error.value = "HTTP error: ${result.message}"
                }

                is ApiResult.NetworkError -> {
                    Log.e(TAG, "addSongToPlaylist Network error", result.exception)
                    _error.value = "Error de red al añadir canción"
                }

                is ApiResult.UnknownError -> {
                    Log.e(TAG, "addSongToPlaylist Unknown error", result.exception)
                    _error.value = "Error desconocido al añadir canción"
                }
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Int) {
        viewModelScope.launch {
            when (val result = service.removeSongFromPlaylist(songId.toString(), playlistId)) {
                is ApiResult.Success -> {
                    val currentPlaylists = _playlists.value.orEmpty()
                    val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlistId }

                    if (playlistIndex != -1) {
                        val targetPlaylist = currentPlaylists[playlistIndex]
                        val updatedSongs = targetPlaylist.songs.filterNot { it.id == songId }

                        val updatedPlaylist = targetPlaylist.copy(
                            songs = updatedSongs
                        )

                        val newPlaylists = currentPlaylists.toMutableList().apply {
                            this[playlistIndex] = updatedPlaylist
                        }.toList()

                        _playlists.value = newPlaylists
                    } else {
                        Log.w(
                            TAG,
                            "removeSongFromPlaylist: Playlist with ID $playlistId not found."
                        )
                    }
                }

                is ApiResult.HttpError -> {
                    Log.e(TAG, "removeSongFromPlaylist HTTP error: ${result.message}")
                    _error.value = "HTTP error: ${result.message}"
                }

                is ApiResult.NetworkError -> {
                    Log.e(TAG, "removeSongFromPlaylist Network error", result.exception)
                    _error.value = "Error de red al eliminar canción"
                }

                is ApiResult.UnknownError -> {
                    Log.e(TAG, "removeSongFromPlaylist Unknown error", result.exception)
                    _error.value = "Error desconocido al eliminar canción"
                }
            }
        }
    }

    fun editPlaylist(id: String, newName: String, newDescription: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = service.editPlaylist(id, newName, newDescription)) {
                is ApiResult.Success -> {
                    loadPlaylists()
                }

                is ApiResult.HttpError -> _error.postValue("Error al editar: ${result.message}")
                is ApiResult.NetworkError -> _error.postValue("Problema de red")
                is ApiResult.UnknownError -> _error.postValue("Error inesperado al editar")
            }
        }
    }


    fun clearError() {
        _error.value = null
    }
}