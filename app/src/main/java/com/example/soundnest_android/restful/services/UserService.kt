package restful.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import restful.models.user.AdditionalInformation
import restful.models.user.EditUserRequest
import restful.models.user.NewUserRequest
import restful.services.interfaces.IAuthService
import restful.services.interfaces.IUserService
import restful.utils.ApiResult
import restful.utils.createUnsafeOkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class UserService(baseUrl: String) : BaseService(baseUrl) {
    private val userService = retrofit.create(IUserService::class.java)

    suspend fun createUser(
        username: String,
        email: String,
        password: String,
        code: String,
        additionalInformation: AdditionalInformation
    ): ApiResult<Unit?> {
        val newUserRequest = NewUserRequest(
            nameUser = username,
            email = email,
            password = password,
            code = code,
            additionalInformation = additionalInformation
        )
        return safeCall { userService.createUser(newUserRequest) }
    }

    suspend fun editUser(
        username: String,
        email: String,
        password: String,
        additionalInformation: AdditionalInformation
    ): ApiResult<Unit?> {
        val editUserRequest = restful.models.user.EditUserRequest(
            nameUser = username,
            email = email,
            password = password,
            additionalInformation = additionalInformation
        )
        return safeCall { userService.editUser(editUserRequest) }
    }
}