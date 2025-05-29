package com.example.soundnest_android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.UserImageGrpcService
import io.grpc.Status
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

const val TOKKEN_JWT_USER_IMAGE =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywidXNlcm5hbWUiOiIyIiwiZW1haWwiOiJ6czIyMDEzNjk4YWFAZXN0dWRpYW50ZXMudXYubXgiLCJyb2xlIjoyLCJpYXQiOjE3NDczMzg5MTgsImV4cCI6MTc0NzQyMTcxOH0.EiMrrlIka26PcID1J72R6RN32ExPsMRVvEaKjcx7pKQ"

class UserImageGrpcServiceIntegrationTest {
    @Test
    fun uploadImageRealCall() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val context = ApplicationProvider.getApplicationContext<Context>()
        //Precondition:
        // Valid image in your pc
        // Valid PC in your database
        val inputStream = context.assets.open("imagen.jpg");
        val imageData = inputStream.readBytes()
        val extension = "jpg"
        val userId = 3

        val service = UserImageGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { TOKKEN_JWT_USER_IMAGE }
        val result = service.uploadImage(userId, imageData, extension)

        when (result) {
            is GrpcResult.Success -> {
                println("Nice")
            }

            is GrpcResult.GrpcError -> fail("HTTP error: ${result.statusCode} - ${result.message}")
            is GrpcResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is GrpcResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }

        assertTrue(result is com.example.soundnest_android.grpc.http.GrpcResult.Success)
    }

    @Test
    fun downloadImageRealCall() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val service = UserImageGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { TOKKEN_JWT_USER_IMAGE }

        val result = service.downloadImage(1)
        assertTrue(result is com.example.soundnest_android.grpc.http.GrpcResult.Success)
        val successResult = result as com.example.soundnest_android.grpc.http.GrpcResult.Success
        val downloadResponse = successResult.data
        assertNotNull(downloadResponse)
        assertTrue(downloadResponse!!.imageData.size() > 0)
        println("Downloaded ${downloadResponse?.imageData?.size()} bytes")
    }

    @Test
    fun downloadNoImageMustFail() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val service = UserImageGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { TOKKEN_JWT_USER_IMAGE }

        val result = service.downloadImage(3)
        when (result) {
            is GrpcResult.Success -> {
                fail("Se esperaba error NOT_FOUND, pero vino Success")
            }

            is GrpcResult.GrpcError -> {
                assertEquals(
                    Status.Code.NOT_FOUND,
                    result.statusCode
                )
            }

            is GrpcResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is GrpcResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }

    }
}