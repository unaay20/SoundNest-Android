package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.auth.FcmTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.soundnest_android.restful.models.auth.LoginRequest
import com.example.soundnest_android.restful.models.auth.LoginResponse
import com.example.soundnest_android.restful.models.auth.SendCodeRequest
import com.example.soundnest_android.restful.models.auth.VerifyCodeRequest

interface IAuthService {
    @POST(RestfulRoutes.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST(RestfulRoutes.AUTH_SEND_CODE_EMAIL)
    suspend fun sendCode(@Body request: SendCodeRequest): Response<Unit>
    @POST(RestfulRoutes.AUTH_VERIFY_CODE)
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<Unit>
    @POST(RestfulRoutes.AUTH_UPDATE_FCM_TOKEN)
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<Unit>
}