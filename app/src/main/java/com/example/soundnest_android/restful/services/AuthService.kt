package restful.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import restful.models.auth.LoginRequest
import restful.models.auth.LoginResponse
import restful.models.auth.SendCodeRequest
import restful.models.auth.VerifyCodeRequest
import restful.services.interfaces.IAuthService
import restful.utils.ApiResult

class AuthService(baseUrl: String) : BaseService(baseUrl) {
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
}
