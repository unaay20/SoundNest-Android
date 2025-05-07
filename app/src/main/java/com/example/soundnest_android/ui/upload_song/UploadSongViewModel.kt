package com.example.soundnest_android.ui.upload_song

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UploadSongViewModel : ViewModel() {
    companion object {
        const val REQUEST_CODE_PICK_FILE = 1001
    }

    private val _fileUri = MutableLiveData<Uri?>()
    val fileUri: LiveData<Uri?> = _fileUri

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    fun onSelectFile(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
        }
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    fun onFilePicked(uri: Uri) {
        _fileUri.value = uri
    }

    fun onUploadClicked(name: String, description: String, genre: String) {
        if (name.isBlank() || _fileUri.value == null) {
            return
        }
        // TODO: implementar llamada al repositorio/servicio para subir la canción
        // Por ahora simulamos éxito inmediato:
        _uploadSuccess.value = true
    }
}
