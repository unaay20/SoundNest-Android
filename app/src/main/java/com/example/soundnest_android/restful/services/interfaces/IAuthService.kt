package restful.services.interfaces

import restful.constants.ApiRoutes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import restful.models.auth.LoginRequest
import restful.models.auth.LoginResponse
import restful.models.auth.SendCodeRequest
import restful.models.auth.VerifyCodeRequest

interface IAuthService {
    @POST(ApiRoutes.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST(ApiRoutes.AUTH_SEND_CODE_EMAIL)
    suspend fun sendCode(@Body request: SendCodeRequest): Response<Unit>
    @POST(ApiRoutes.AUTH_VERIFY_CODE)
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<Unit>
}