package restful.models.user

import com.squareup.moshi.Json

data class EditUserRequest(
    @Json(name = "nameUser")
    val nameUser: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "additionalInformation")
    val additionalInformation: restful.models.user.AdditionalInformation
)