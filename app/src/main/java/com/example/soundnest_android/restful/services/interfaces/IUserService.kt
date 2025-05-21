package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.user.EditUserPasswordRequest
import com.example.soundnest_android.restful.models.user.EditUserRequest
import com.example.soundnest_android.restful.models.user.NewUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface IUserService {
    @POST(RestfulRoutes.USER_NEW_USER)
    suspend fun createUser(@Body request: NewUserRequest): Response<Unit>

    @PATCH(RestfulRoutes.USER_EDIT_USER)
    suspend fun editUser(@Body request: EditUserRequest): Response<Unit>

    @PATCH(RestfulRoutes.USER_EDIT_PASSWORD)
    suspend fun editUserPassword(@Body request: EditUserPasswordRequest): Response<Unit>
}