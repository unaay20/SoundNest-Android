package com.example.soundnest_android.auth

import com.example.soundnest_android.network.ApiClient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.soundnest_android.models.LoginRequest
import com.example.soundnest_android.models.LoginResponse
import com.example.soundnest_android.models.RegisterRequest
import com.example.soundnest_android.models.RegisterResponse

interface IAuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/user/newUser")
    suspend fun createUser(@Body request: RegisterRequest): Response<RegisterResponse>
}

val authService: IAuthService = ApiClient.retrofit.create(IAuthService::class.java)
