package com.example.soundnest_android.restful.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.AuthInterceptor
import com.example.soundnest_android.restful.utils.TokenProvider
import com.example.soundnest_android.restful.utils.createUnsafeOkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

open class BaseService(baseUrl : String, tokenProvider: TokenProvider? = null) {
    protected val retrofit : Retrofit
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val errorAdapter = moshi.adapter(ErrorResponse::class.java)
    data class ErrorResponse(val message: String)

    init {
        val authInterceptor = AuthInterceptor(tokenProvider)
        val client = createUnsafeOkHttpClient()
            .newBuilder()
            .addInterceptor(authInterceptor)
            .build()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    protected suspend fun <T> safeCall(apiCall: suspend () -> Response<T>): ApiResult<T?> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                ApiResult.Success(response.body())
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorBody(errorBody) ?: "HTTP ${response.code()} - No error body"
                ApiResult.HttpError(response.code(), errorMessage)
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    private fun parseErrorBody(errorBody: String?): String? {
        return try {
            if (!errorBody.isNullOrBlank()) {
                val errorResponse = errorAdapter.fromJson(errorBody)
                errorResponse?.message
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}