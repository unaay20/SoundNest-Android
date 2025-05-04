package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.ApiRoutes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.soundnest_android.restful.models.auth.LoginRequest
import com.example.soundnest_android.restful.models.auth.LoginResponse
import com.example.soundnest_android.restful.models.auth.SendCodeRequest
import com.example.soundnest_android.restful.models.auth.VerifyCodeRequest

interface IAuthService {
    @POST(ApiRoutes.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST(ApiRoutes.AUTH_SEND_CODE_EMAIL)
    suspend fun sendCode(@Body request: SendCodeRequest): Response<Unit>
    @POST(ApiRoutes.AUTH_VERIFY_CODE)
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<Unit>
}