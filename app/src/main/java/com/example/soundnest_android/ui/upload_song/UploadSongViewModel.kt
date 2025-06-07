package com.example.soundnest_android.ui.upload_song

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.models.song.GenreResponse
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import com.example.soundnest_android.song.UploadSongResponse
import kotlinx.coroutines.launch
import java.util.Base64

class UploadSongViewModel(
    private val grpcService: SongFileGrpcService,
    private val restService: SongService,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    companion object {
        const val REQUEST_CODE_PICK_FILE = 1001
        const val REQUEST_CODE_PICK_IMAGE = 1002
    }

    private val _fileUri = MutableLiveData<Uri?>()
    val fileUri: LiveData<Uri?> = _fileUri

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val _genres = MutableLiveData<List<GenreResponse>?>()
    val genres: LiveData<List<GenreResponse>?> = _genres

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _uploadError = MutableLiveData<String?>()
    val uploadError: LiveData<String?> = _uploadError

    private val _uploadedSongId = MutableLiveData<Int?>()
    val uploadedSongId: LiveData<Int?> = _uploadedSongId

    fun fetchGenres() {
        viewModelScope.launch {
            when (val result = restService.getGenres()) {
                is ApiResult.Success -> _genres.postValue(result.data)
                is ApiResult.HttpError -> TODO()
                is ApiResult.NetworkError -> TODO()
                is ApiResult.UnknownError -> TODO()
            }
        }
    }

    fun onSelectFile(activity: android.app.Activity) {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
            .apply { type = "audio/*" }
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    fun onSelectImage(activity: android.app.Activity) {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
            .apply { type = "image/*" }
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    fun onFilePicked(uri: Uri) {
        _fileUri.value = uri
    }

    fun onImagePicked(uri: Uri) {
        _imageUri.value = uri
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onUploadClicked(
        context: Context,
        songName: String,
        description: String,
        genreId: Int
    ) {
        val uri = _fileUri.value ?: run {
            _uploadError.value = "Falta seleccionar fichero"
            return
        }
        if (songName.isBlank()) {
            _uploadError.value = "El nombre no puede estar vacío"
            return
        }
        if (!isValidMp3(context, uri)) {
            _uploadError.value = "El fichero no es un MP3 válido"
            return
        }

        viewModelScope.launch {
            when (val grpcResult = grpcService.uploadSong(
                songName = songName,
                songGenreId = genreId,
                description = description,
                extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    ?: "mp3",
                fileData = context.contentResolver.openInputStream(uri)!!.readBytes()
            )) {
                is GrpcResult.Success<*> -> {
                    val resp = grpcResult.data as? UploadSongResponse
                    if (resp?.result == true) {
                        fetchLatestAndUploadImage(context)
                    } else {
                        _uploadSuccess.postValue(false)
                        _uploadError.postValue(resp?.message ?: "Error en servidor gRPC")
                    }
                }

                is GrpcResult.GrpcError -> {
                    _uploadError.postValue("gRPC Error ${grpcResult.statusCode}")
                }

                is GrpcResult.NetworkError -> {
                    _uploadError.postValue("Network Error")
                }

                is GrpcResult.UnknownError -> {
                    _uploadError.postValue("Unknown Error")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadSongImage(context: Context) {
        viewModelScope.launch {
            fetchLatestAndUploadImage(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchLatestAndUploadImage(context: Context) {
        val userId = (tokenProvider as? SharedPrefsTokenProvider)?.id ?: return
        when (val result = restService.getLatest(userId)) {
            is ApiResult.Success -> {
                val song = result.data
                val songId = song?.idSong ?: run {
                    _uploadError.postValue("No se obtuvo ID")
                    return
                }

                _imageUri.value?.let { uri ->
                    val bytes = context.contentResolver.openInputStream(uri)!!.readBytes()
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val base64 = "data:$mime;base64," + Base64.getEncoder().encodeToString(bytes)
                    when (val up = restService.uploadSongImage(songId, base64)) {
                        is ApiResult.Success -> {
                            _uploadSuccess.postValue(true)
                        }

                        is ApiResult.HttpError -> {
                            _uploadError.postValue("Error HTTP al obtener última canción")
                            _uploadSuccess.postValue(false)
                        }

                        is ApiResult.NetworkError -> {
                            _uploadError.postValue("Error de red al obtener última canción")
                            _uploadSuccess.postValue(false)
                        }

                        is ApiResult.UnknownError -> {
                            _uploadError.postValue("Error desconocido al obtener última canción")
                            _uploadSuccess.postValue(false)
                        }
                    }
                    _uploadSuccess.postValue(true)
                }
            }

            is ApiResult.HttpError -> {
                _uploadError.postValue("Error HTTP al obtener última canción")
                _uploadSuccess.postValue(false)
            }

            is ApiResult.NetworkError -> {
                _uploadError.postValue("Error de red al obtener última canción")
                _uploadSuccess.postValue(false)
            }

            is ApiResult.UnknownError -> {
                _uploadError.postValue("Error desconocido al obtener última canción")
                _uploadSuccess.postValue(false)
            }
        }
    }

    private fun isValidMp3(context: Context, uri: Uri): Boolean {
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            dur > 0L
        } catch (e: Exception) {
            false
        }
    }

    fun resetUploadSuccess() {
        _uploadSuccess.postValue(false)
    }
}