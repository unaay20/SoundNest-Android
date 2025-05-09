package com.example.soundnest_android.grpc.services


import android.content.Context
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.user_image.DownloadImageRequest
import com.example.soundnest_android.user_image.DownloadImageResponse
import com.example.soundnest_android.user_image.UploadImageRequest
import com.example.soundnest_android.user_image.UploadImageResponse
import com.example.soundnest_android.user_image.UserImageServiceGrpcKt.UserImageServiceCoroutineStub
import com.google.protobuf.ByteString

class UserImageGrpcService(
    host: String,
    port: Int, tokenProvider: () -> String?
) : BaseGrpcService(host, port, tokenProvider) {
    private val stub = UserImageServiceCoroutineStub(channel)

    suspend fun uploadImage(userId: Int, imageData: ByteArray, extension: String): GrpcResult<UploadImageResponse?> {
        val request = UploadImageRequest.newBuilder()
            .setUserId(userId)
            .setImageData(ByteString.copyFrom(imageData))
            .setExtension(extension)
            .build()

        return safeCall { stub.uploadImage(request) }
    }
    suspend fun downloadImage(userId: Int): GrpcResult<DownloadImageResponse?>{
        val request = DownloadImageRequest.newBuilder()
            .setUserId(userId)
            .build()

        return  safeCall { stub.downloadImage(request) }

    }
}
