package com.example.soundnest_android.ui.playlists

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.R
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
    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

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
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val result = service.fetchByUser(userId)
            withContext(Dispatchers.Main) {
                _isLoading.value = false
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
                                        coverUrl = null,
                                        duration = 0,
                                        releaseDate = "",
                                        description = null
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
                        _error.value = getApplication<Application>().getString(
                            R.string.err_loading_playlists,
                            result.message
                        )

                    }

                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "Network error", result.exception)
                        _error.value =
                            getApplication<Application>().getString(R.string.err_network_loading_playlists)

                    }

                    is ApiResult.UnknownError -> {
                        Log.e(TAG, "Unknown error", result.exception)
                        _error.value =
                            getApplication<Application>().getString(R.string.err_unknown_loading_playlists)

                    }
                }
            }
        }
    }

    fun createPlaylist(name: String, description: String, imageUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                if (name.isBlank() || description.isBlank()) {
                    withContext(Dispatchers.Main) {
                        _error.value =
                            getApplication<Application>().getString(R.string.err_invalid_playlist_data)
                        _isLoading.value = false
                    }
                    return@launch
                }

                if (imageUri.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _error.value =
                            getApplication<Application>().getString(R.string.err_missing_image)
                        _isLoading.value = false
                    }
                    return@launch
                }

                val imageFile = try {
                    uriToFile(imageUri)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting URI to file", e)
                    withContext(Dispatchers.Main) {
                        _error.value =
                            getApplication<Application>().getString(R.string.err_processing_image)
                        _isLoading.value = false
                    }
                    return@launch
                }

                if (!imageFile.exists() || imageFile.length() == 0L) {
                    withContext(Dispatchers.Main) {
                        _error.value =
                            getApplication<Application>().getString(R.string.err_invalid_image_file)
                        _isLoading.value = false
                    }
                    return@launch
                }

                val mimeType = getMimeType(imageFile) ?: "image/jpeg"
                val requestBody = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val imagePart =
                    MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d(
                    TAG,
                    "Creating playlist: name=$name, desc=$description, file=${imageFile.name}"
                )

                val result = service.createPlaylist(imagePart, namePart, descPart)

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    when (result) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "Playlist created successfully")
                            _error.value = null
                            loadPlaylists()
                        }

                        is ApiResult.HttpError -> {
                            Log.e(TAG, "HTTP error creating playlist: ${result.message}")
                            _error.value = getApplication<Application>().getString(
                                R.string.err_http_create_playlist,
                                result.message
                            )

                        }

                        is ApiResult.NetworkError -> {
                            Log.e(TAG, "Network error creating playlist", result.exception)
                            _error.value =
                                getApplication<Application>().getString(R.string.err_network_create_playlist)

                        }

                        is ApiResult.UnknownError -> {
                            Log.e(TAG, "Unknown error creating playlist", result.exception)
                            _error.value =
                                getApplication<Application>().getString(R.string.err_unknown_create_playlist)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in createPlaylist", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _error.value = getApplication<Application>().getString(
                        R.string.err_unexpected_create_playlist,
                        e.message ?: ""
                    )
                }
            }
        }
    }

    private fun uriToFile(uriString: String): File {
        val uri = Uri.parse(uriString)
        val context = getApplication<Application>()

        return when (uri.scheme) {
            "content" -> {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Cannot open input stream for URI")

                // Crear archivo temporal
                val tempFile = File.createTempFile("playlist_image", ".jpg", context.cacheDir)
                tempFile.outputStream().use { output ->
                    inputStream.use { input ->
                        input.copyTo(output)
                    }
                }
                tempFile
            }

            "file" -> File(uri.path ?: throw IllegalArgumentException("Invalid file URI"))
            else -> throw IllegalArgumentException("Unsupported URI scheme: ${uri.scheme}")
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
                        _error.value = getApplication<Application>().getString(
                            R.string.err_http_delete_playlist,
                            result.message
                        )
                    }

                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "Network error deleting playlist", result.exception)
                        _error.value =
                            getApplication<Application>().getString(R.string.err_network_delete_playlist)
                    }

                    is ApiResult.UnknownError -> {
                        Log.e(TAG, "Unknown error deleting playlist", result.exception)
                        _error.value =
                            getApplication<Application>().getString(R.string.err_unknown_delete_playlist)
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
                            Song(
                                songId, /* title… */
                                "Unknown Title",
                                "Unknown Artist",
                                null,
                                0,
                                "",
                                null
                            )

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
                    _error.value = getApplication<Application>().getString(
                        R.string.err_http_create_playlist,
                        result.message
                    )
                }

                is ApiResult.NetworkError -> {
                    Log.e(TAG, "addSongToPlaylist Network error", result.exception)
                    _error.value =
                        getApplication<Application>().getString(R.string.err_network_add_song)
                }

                is ApiResult.UnknownError -> {
                    Log.e(TAG, "addSongToPlaylist Unknown error", result.exception)
                    _error.value =
                        getApplication<Application>().getString(R.string.err_unknown_add_song)
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
                    _error.value = getApplication<Application>().getString(
                        R.string.err_http_create_playlist,
                        result.message
                    )
                }

                is ApiResult.NetworkError -> {
                    Log.e(TAG, "removeSongFromPlaylist Network error", result.exception)
                    _error.value =
                        getApplication<Application>().getString(R.string.err_network_remove_song)
                }

                is ApiResult.UnknownError -> {
                    Log.e(TAG, "removeSongFromPlaylist Unknown error", result.exception)
                    _error.value =
                        getApplication<Application>().getString(R.string.err_unknown_remove_song)
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

                is ApiResult.HttpError -> _error.postValue(
                    getApplication<Application>().getString(
                        R.string.err_http_edit_playlist,
                        result.message
                    )
                )

                is ApiResult.NetworkError -> _error.postValue(
                    getApplication<Application>().getString(
                        R.string.err_network_edit_playlist
                    )
                )

                is ApiResult.UnknownError -> _error.postValue(
                    getApplication<Application>().getString(
                        R.string.err_unknown_edit_playlist
                    )
                )
            }
        }
    }


    fun clearError() {
        _error.value = null
    }
}