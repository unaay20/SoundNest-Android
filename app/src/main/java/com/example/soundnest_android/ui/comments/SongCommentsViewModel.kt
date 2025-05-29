package com.example.soundnest_android.ui.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.business_logic.Comment
import com.example.soundnest_android.data.CommentRepository
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import kotlinx.coroutines.launch

class SongCommentsViewModel(
    tokenProvider: TokenProvider
) : ViewModel() {
    private val repo = CommentRepository(
        CommentService(RestfulRoutes.getBaseUrl(), tokenProvider)
    )

    private var _songId: Int? = null

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun setSongId(songId: Int) {
        _songId = songId
    }

    fun loadComments() = viewModelScope.launch {
        _songId?.let { songId ->
            when (val result = repo.getCommentsForSong(songId.toString())) {
                is ApiResult.Success -> {
                    _comments.value = result.data ?: emptyList(); _error.value = null
                }

                is ApiResult.HttpError -> _error.value = "Error ${result.code}: ${result.message}"
                is ApiResult.NetworkError -> _error.value =
                    "Fallo de red: ${result.exception.message}"

                is ApiResult.UnknownError -> _error.value =
                    "Error desconocido: ${result.exception.message}"
            }
        }
    }

    fun addComment(songId: Int, user: String, text: String) = viewModelScope.launch {
        val request = CreateCommentRequest(songId, text)
        when (val result = repo.createComment(request)) {
            is ApiResult.Success -> {
                loadComments(); _error.value = null
            }

            is ApiResult.HttpError -> _error.value = "Error ${result.code}: ${result.message}"
            is ApiResult.NetworkError -> _error.value = "Fallo de red: ${result.exception.message}"
            is ApiResult.UnknownError -> _error.value =
                "Error desconocido: ${result.exception.message}"
        }
    }

    fun deleteComment(commentId: String) = viewModelScope.launch {
        when (val result = repo.removeComment(commentId)) {
            is ApiResult.Success -> loadComments()
            is ApiResult.HttpError -> _error.value = "Error ${result.code}: ${result.message}"
            is ApiResult.NetworkError -> _error.value = "Fallo de red: ${result.exception.message}"
            is ApiResult.UnknownError -> _error.value =
                "Error desconocido: ${result.exception.message}"
        }
    }

    fun replyToComment(parentCommentId: String, message: String) = viewModelScope.launch {
        when (val result = repo.respondToComment(parentCommentId, message)) {
            is ApiResult.Success -> {
                loadComments(); _error.value = null
            }

            is ApiResult.HttpError -> _error.value = "Error ${result.code}: ${result.message}"
            is ApiResult.NetworkError -> _error.value = "Fallo de red: ${result.exception.message}"
            is ApiResult.UnknownError -> _error.value =
                "Error desconocido: ${result.exception.message}"
        }
    }
}
