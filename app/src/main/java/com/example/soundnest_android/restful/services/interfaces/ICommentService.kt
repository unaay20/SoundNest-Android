package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.comment.CommentResponse
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.models.comment.RespondCommentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ICommentService {
    @POST(RestfulRoutes.COMMENT_CREATE)
    suspend fun createComment(@Body comment: CreateCommentRequest): Response<Unit>

    @GET(RestfulRoutes.COMMENT_GET_BY_SONG_ID)
    suspend fun getAllCommentsByIdSong(@Path("song_id") songId: String): Response<List<CommentResponse>>

    @GET(RestfulRoutes.COMMENT_GET_BY_ID)
    suspend fun getCommentById(@Path("id") commentId: String): Response<CommentResponse>

    @DELETE(RestfulRoutes.COMMENT_DELETE)
    suspend fun deleteComment(@Path("id") commentId: String): Response<Unit>

    @POST(RestfulRoutes.COMMENT_RESPOND)
    suspend fun respondComment(
        @Path("commentId") commentId: String,
        @Body request: RespondCommentRequest
    ): Response<Unit>
}