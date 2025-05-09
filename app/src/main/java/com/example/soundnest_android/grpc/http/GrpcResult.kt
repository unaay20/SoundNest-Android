package com.example.soundnest_android.grpc.http

import java.io.IOException

sealed class GrpcResult<out T> {
    data class Success<out T>(val data: T?) : GrpcResult<T>()
    data class GrpcError(val statusCode: io.grpc.Status.Code, val message: String) : GrpcResult<Nothing>()
    data class NetworkError(val exception: IOException) : GrpcResult<Nothing>()
    data class UnknownError(val exception: Exception) : GrpcResult<Nothing>()
}