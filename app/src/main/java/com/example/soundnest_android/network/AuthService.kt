package com.example.soundnest_android.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val userId: String)

// --- NUEVO: request/response de registro
data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(val token: String, val userId: String)

interface AuthService {
    @POST("login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    // ← Endpoint de registro
    @POST("register")
    suspend fun register(@Body req: RegisterRequest): Response<RegisterResponse>
}

val authService: AuthService = ApiClient.retrofit.create(AuthService::class.java)
