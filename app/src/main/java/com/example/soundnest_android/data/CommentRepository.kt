package com.example.soundnest_android.data

import com.example.soundnest_android.restful.models.comment.CommentResponse
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.comments.Comment

class CommentRepository(
    private val service: CommentService
) {

    suspend fun getCommentsForSong(songId: String): ApiResult<List<Comment>> {
        return when (val apiResult = service.fetchComments(songId)) {
            is ApiResult.Success -> {
                val domainComments = apiResult.data
                    ?.map { it.toDomain() }
                    ?: emptyList()
                ApiResult.Success(domainComments)
            }
            is ApiResult.HttpError   -> ApiResult.HttpError(apiResult.code, apiResult.message)
            is ApiResult.NetworkError-> ApiResult.NetworkError(apiResult.exception)
            is ApiResult.UnknownError-> ApiResult.UnknownError(apiResult.exception)
        }
    }

    suspend fun createComment(request: CreateCommentRequest): ApiResult<Unit?> {
        return service.postComment(request)
    }

    suspend fun removeComment(commentId: String): ApiResult<Unit?> {
        return service.removeComment(commentId)
    }

    suspend fun respondToComment(commentId: String, message: String): ApiResult<Unit?> {
        return service.respondComment(commentId, message)
    }

    private fun CommentResponse.toDomain(): Comment =
        Comment(
            id        = this.id,
            songId    = this.songId,
            user      = this.user,
            message   = this.message,
            parentId  = this.parentId,
            timestamp = this.timestamp
        )
}
