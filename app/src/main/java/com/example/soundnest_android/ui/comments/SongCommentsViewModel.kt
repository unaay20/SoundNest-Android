package com.example.soundnest_android.ui.comments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.business_logic.Comment
import com.example.soundnest_android.data.CommentRepository
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import com.example.soundnest_android.utils.toDisplayMessage
import kotlinx.coroutines.launch

class SongCommentsViewModel(
    application: Application,
    tokenProvider: TokenProvider
) : AndroidViewModel(application) {

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
                    _comments.value = result.data ?: emptyList()
                    _error.value = null
                }

                else -> {
                    _error.value = result.toDisplayMessage(getApplication())
                }
            }
        }
    }

    fun addComment(songId: Int, user: String, text: String) = viewModelScope.launch {
        val request = CreateCommentRequest(songId, text)
        when (val result = repo.createComment(request)) {
            is ApiResult.Success -> {
                loadComments()
                _error.value = null
            }

            else -> {
                _error.value = result.toDisplayMessage(getApplication())
            }
        }
    }

    fun deleteComment(commentId: String) = viewModelScope.launch {
        when (val result = repo.removeComment(commentId)) {
            is ApiResult.Success -> loadComments()
            else -> _error.value = result.toDisplayMessage(getApplication())
        }
    }

    fun replyToComment(parentCommentId: String, message: String) = viewModelScope.launch {
        when (val result = repo.respondToComment(parentCommentId, message)) {
            is ApiResult.Success -> {
                loadComments()
                _error.value = null
            }

            else -> {
                _error.value = result.toDisplayMessage(getApplication())
            }
        }
    }
}
