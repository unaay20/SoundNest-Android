package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.auth.FcmTokenRequest
import com.example.soundnest_android.restful.models.auth.LoginRequest
import com.example.soundnest_android.restful.models.auth.LoginResponse
import com.example.soundnest_android.restful.models.auth.SendCodeRequest
import com.example.soundnest_android.restful.models.auth.VerifyCodeRequest
import com.example.soundnest_android.restful.services.interfaces.IAuthService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider

class AuthService(baseUrl: String, tokenProvider: TokenProvider? = null) :
    BaseService(baseUrl, tokenProvider) {
    private val authService = retrofit.create(IAuthService::class.java)

    suspend fun login(username: String, password: String): ApiResult<LoginResponse?> {
        return safeCall { authService.login(LoginRequest(username, password)) }
    }

    suspend fun sendCodeToEmail(email: String): ApiResult<Unit?> {
        return safeCall { authService.sendCode(SendCodeRequest(email)) }
    }

    suspend fun verifyCode(email: String, code: Int): ApiResult<Unit?> {
        return safeCall { authService.verifyCode(VerifyCodeRequest(email, code)) }
    }

    suspend fun updateFcmToken(
        token: String,
        device: String? = "android",
        platformVersion: String? = null,
        appVersion: String? = null
    ): ApiResult<Unit?> {
        val request = FcmTokenRequest(
            token = token,
            device = device,
            platformVersion = platformVersion,
            appVersion = appVersion
        )
        return safeCall { authService.updateFcmToken(request) }
    }
}
