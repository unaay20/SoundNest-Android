package restful.models.auth

import com.squareup.moshi.Json

data class LoginResponse(
    @Json(name = "token")
    val token: String
)