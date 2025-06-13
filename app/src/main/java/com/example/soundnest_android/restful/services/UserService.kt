package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.user.AdditionalInformation
import com.example.soundnest_android.restful.models.user.EditUserPasswordRequest
import com.example.soundnest_android.restful.models.user.EditUserRequest
import com.example.soundnest_android.restful.models.user.NewUserRequest
import com.example.soundnest_android.restful.services.interfaces.IUserService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider

class UserService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {
    private val userService = retrofit.create(IUserService::class.java)

    suspend fun createUser(
        username: String,
        email: String,
        password: String,
        code: String,
        additionalInformation: String
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
        additionalInformation: String
    ): ApiResult<Unit?> {
        val editUserRequest = EditUserRequest(
            nameUser = username,
            email = email,
            additionalInformation = additionalInformation
        )
        return safeCall { userService.editUser(editUserRequest) }
    }

    suspend fun editUserPassword(
        email: String,
        code: String,
        newPassword: String
    ): ApiResult<Unit?> {
        val editUserPasswordRequest = EditUserPasswordRequest(
            email = email,
            code = code,
            newPassword = newPassword
        )
        return safeCall { userService.editUserPassword(editUserPasswordRequest) }
    }

    suspend fun getAdditionalInfo(): ApiResult<AdditionalInformation?> {
        return safeCall { userService.getAdditionalInfo() }
    }
}