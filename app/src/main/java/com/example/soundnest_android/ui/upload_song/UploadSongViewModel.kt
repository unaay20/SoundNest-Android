package com.example.soundnest_android.ui.upload_song

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.song.UploadSongResponse
import kotlinx.coroutines.launch

class UploadSongViewModel(
    private val grpcService: SongFileGrpcService
) : ViewModel() {
    companion object {
        const val REQUEST_CODE_PICK_FILE = 1001
    }

    private val _fileUri = MutableLiveData<Uri?>()
    val fileUri: LiveData<Uri?> = _fileUri

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _uploadError = MutableLiveData<String?>()
    val uploadError: LiveData<String?> = _uploadError

    fun onSelectFile(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "audio/*" }
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    fun onFilePicked(uri: Uri) {
        _fileUri.value = uri
    }

    fun onUploadClicked(
        context: Context,
        songName: String,
        description: String,
        genreString: String
    ) {
        val uri = _fileUri.value
        if (songName.isBlank() || uri == null) {
            _uploadError.value = "Nombre o fichero inválido"
            return
        }

        val genreId = mapGenreToId(genreString)

        viewModelScope.launch {
            val grpcResult = grpcService.uploadSong(
                songName = songName,
                songGenreId = genreId,
                description = description,
                extension = extractExtension(context, uri),
                fileData = readBytes(context, uri)
            )

            when (grpcResult) {
                is GrpcResult.Success<*> -> {
                    val response = grpcResult.data as? UploadSongResponse
                    if (response == null) {
                        _uploadSuccess.postValue(false)
                        _uploadError.postValue("Respuesta vacía del servidor")
                    } else if (response.result) {
                        _uploadSuccess.postValue(true)
                        _uploadError.postValue(null)
                    } else {
                        _uploadSuccess.postValue(false)
                        _uploadError.postValue(response.message)
                    }
                }
                is GrpcResult.GrpcError -> {
                    _uploadSuccess.postValue(false)
                    _uploadError.postValue("gRPC Error ${grpcResult.statusCode}: ${grpcResult.message}")
                }
                is GrpcResult.NetworkError -> {
                    _uploadSuccess.postValue(false)
                    _uploadError.postValue("Network Error: ${grpcResult.exception.localizedMessage}")
                }
                is GrpcResult.UnknownError -> {
                    _uploadSuccess.postValue(false)
                    _uploadError.postValue("Unknown Error: ${grpcResult.exception.localizedMessage}")
                }
            }
        }
    }
    private fun mapGenreToId(genre: String): Int = when (genre) {
        "Rock"  -> 1
        "Pop"   -> 2
        "Indie" -> 3
        else    -> 0
    }

    private fun extractExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mime) ?: "mp3"
    }

    private fun readBytes(context: Context, uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }

}

