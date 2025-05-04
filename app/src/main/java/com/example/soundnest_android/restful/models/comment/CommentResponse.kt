package restful.models.comment

import com.squareup.moshi.Json

data class CommentResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "song_id")
    val songId: Int,

    @Json(name = "user")
    val user: String,

    @Json(name = "message")
    val message: String
)
