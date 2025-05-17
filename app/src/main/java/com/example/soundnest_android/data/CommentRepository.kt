package com.example.soundnest_android.data

import com.example.soundnest_android.restful.models.comment.CommentResponse
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.business_logic.Comment

class CommentRepository(
    private val service: CommentService
) {

    private fun mapResponse(resp: CommentResponse): Comment =
        Comment(
            id        = resp.id,
            songId    = resp.songId,
            user      = resp.user,
            message   = resp.message,
            parentId  = resp.parentId,
            timestamp = resp.timestamp,
            responses = resp.responses
                ?.map { mapResponse(it) }
                ?: emptyList()
        )

    suspend fun getCommentsForSong(songId: String): ApiResult<List<Comment>> {
        return when(val apiResult = service.fetchComments(songId)) {
            is ApiResult.Success -> {
                val domain = apiResult.data
                    ?.map { mapResponse(it) }
                    ?: emptyList()
                ApiResult.Success(domain)
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
