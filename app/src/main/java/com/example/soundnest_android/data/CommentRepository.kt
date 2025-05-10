// data/CommentRepository.kt
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
                    ?.map { resp: CommentResponse ->
                        Comment(
                            song_id = resp.id.toInt(),
                            user = resp.user,
                            message = resp.message
                        )
                    }
                    ?: emptyList()
                ApiResult.Success(domainComments)
            }
            is ApiResult.HttpError -> {
                ApiResult.HttpError(
                    code    = apiResult.code,
                    message = apiResult.message
                )
            }
            is ApiResult.NetworkError -> {
                ApiResult.NetworkError(
                    exception = apiResult.exception
                )
            }
            is ApiResult.UnknownError -> {
                ApiResult.UnknownError(
                    exception = apiResult.exception
                )
            }
        }
    }

    suspend fun createComment(request: CreateCommentRequest): ApiResult<Unit?> {
        return service.postComment(request)
    }

    suspend fun deleteComment(id: String): ApiResult<Unit?> {
        return service.removeComment(id)
    }
}
