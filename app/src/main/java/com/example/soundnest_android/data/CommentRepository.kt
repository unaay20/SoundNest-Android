package com.example.soundnest_android.data

import com.example.soundnest_android.restful.models.comment.CommentResponse
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.comments.Comment
import java.io.IOException

class CommentRepository(
    private val service: CommentService
) {

    suspend fun getCommentsForSong(songId: String): ApiResult<List<Comment>> {
        return when (val apiResult = service.fetchComments(songId)) {
            is ApiResult.Success -> {
                val domainComments = apiResult.data
                    ?.map { resp: CommentResponse ->
                        Comment(
                            song_id = resp.songId,
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

    suspend fun removeComment(commentId: String): ApiResult<Unit?> {
        return try {
            service.removeComment(commentId)
            ApiResult.Success(Unit)
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        }
    }
}
