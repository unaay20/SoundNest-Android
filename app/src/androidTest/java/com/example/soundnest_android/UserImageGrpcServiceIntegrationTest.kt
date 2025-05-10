package com.example.soundnest_android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.services.UserImageGrpcService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class UserImageGrpcServiceIntegrationTest {
    @Test
    fun  uploadImageRealCall() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val context = ApplicationProvider.getApplicationContext<Context>()
        //Precondition:
        // Valid image in your pc
        // Valid PC in your database
        val inputStream = context.assets.open("imagen.jpg");
        val imageData = inputStream.readBytes()
        val extension = "jpg"
        val userId = 1

        val service = UserImageGrpcService(GrpcRoutes.getHost(), GrpcRoutes.getPort()) { "mi-token" }
        val result = service.uploadImage(userId, imageData, extension)

        println("Resultado: $result")

        assertTrue(result is com.example.soundnest_android.grpc.http.GrpcResult.Success)
    }
    @Test
    fun downloadImageRealCall() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val service = UserImageGrpcService(GrpcRoutes.getHost(), GrpcRoutes.getPort()) { "mi-token" }

        val result = service.downloadImage(1)
        assertTrue(result is com.example.soundnest_android.grpc.http.GrpcResult.Success)
        val successResult = result as com.example.soundnest_android.grpc.http.GrpcResult.Success
        val downloadResponse = successResult.data
        assertNotNull(downloadResponse)
        assertTrue(downloadResponse!!.imageData.size() > 0)
        println("Downloaded ${downloadResponse?.imageData?.size()} bytes")
    }
}