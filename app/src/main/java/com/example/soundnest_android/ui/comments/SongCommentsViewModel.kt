package com.example.soundnest_android.ui.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.data.CommentRepository
import com.example.soundnest_android.restful.constants.ApiRoutes.BASE_URL
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.launch

class SongCommentsViewModel : ViewModel() {
    private val repo = CommentRepository(CommentService(BASE_URL))

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadComments(songId: Int) = viewModelScope.launch {
        when (val result = repo.getCommentsForSong(songId.toString())) {
            is ApiResult.Success -> {
                _comments.value = result.data ?: emptyList()
                _error.value = null
            }
            is ApiResult.HttpError -> {
                _error.value = "Error ${result.code}: ${result.message}"
            }
            is ApiResult.NetworkError -> {
                _error.value = "Fallo de red: ${result.exception.message}"
            }
            is ApiResult.UnknownError -> {
                _error.value = "Error desconocido: ${result.exception.message}"
            }
        }
    }

    fun addComment(songId: Int, user: String, text: String) = viewModelScope.launch {
        val request = CreateCommentRequest(
            songId = songId,
            user = user,
            message = text
        )

        when (val result = repo.createComment(request)) {
            is ApiResult.Success -> {
                loadComments(songId)
                _error.value = null
            }
            is ApiResult.HttpError -> {
                _error.value = "Error ${result.code}: ${result.message}"
            }
            is ApiResult.NetworkError -> {
                _error.value = "Fallo de red: ${result.exception.message}"
            }
            is ApiResult.UnknownError -> {
                _error.value = "Error desconocido: ${result.exception.message}"
            }
        }
    }
}

