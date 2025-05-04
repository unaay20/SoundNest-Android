package restful.services.interfaces

import restful.constants.ApiRoutes
import restful.models.comment.CommentResponse
import restful.models.comment.CreateCommentRequest
import retrofit2.Response
import retrofit2.http.*

interface ICommentService {
    @POST(ApiRoutes.COMMENT_CREATE)
    suspend fun createComment(@Body comment: CreateCommentRequest) : Response<Unit>
    @GET(ApiRoutes.COMMENT_GET_BY_SONG_ID)
    suspend fun getAllCommentsByIdSong(@Path("song_id") songId: String): Response<List<CommentResponse>>
    @GET(ApiRoutes.COMMENT_GET_BY_ID)
    suspend fun getCommentById(@Path("id") commentId: String): Response<CommentResponse>
    @DELETE(ApiRoutes.COMMENT_DELETE)
    suspend fun deleteComment(@Path("id") commentId: String): Response<Unit>
}