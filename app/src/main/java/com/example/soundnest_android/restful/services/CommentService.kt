package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.comment.CommentResponse
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.models.comment.RespondCommentRequest
import com.example.soundnest_android.restful.services.interfaces.ICommentService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider


class CommentService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {
    private val commentService = retrofit.create(ICommentService::class.java)

    suspend fun postComment(request: CreateCommentRequest): ApiResult<Unit?> {
        return safeCall { commentService.createComment(request) }
    }

    suspend fun fetchComments(songId: String): ApiResult<List<CommentResponse>?> {
        return safeCall { commentService.getAllCommentsByIdSong(songId) }
    }

    suspend fun fetchCommentById(id: String): ApiResult<CommentResponse?> {
        return safeCall { commentService.getCommentById(id) }
    }

    suspend fun removeComment(id: String): ApiResult<Unit?> {
        return safeCall { commentService.deleteComment(id) }
    }

    suspend fun respondComment(commentId: String, message: String): ApiResult<Unit?> {
        val req = RespondCommentRequest(message)
        return safeCall { commentService.respondComment(commentId, req) }
    }
}