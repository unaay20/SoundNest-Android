package com.example.soundnest_android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

const val TOKEN_JWT =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywidXNlcm5hbWUiOiIyIiwiZW1haWwiOiJ6czIyMDEzNjk4YWFAZXN0dWRpYW50ZXMudXYubXgiLCJyb2xlIjoyLCJpYXQiOjE3NDczMzg5MTgsImV4cCI6MTc0NzQyMTcxOH0.EiMrrlIka26PcID1J72R6RN32ExPsMRVvEaKjcx7pKQ"

class SongFileGrpcServiceIntegrationTest {
    @Test
    fun uploadSongRealCall() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Precondition: valid file
        val inputStream = context.assets.open("test_song.mp3")
        val songData = inputStream.readBytes()

        val songName = "MiCancionDePrueba"
        val songGenreId = 1
        val description = "Una canción de prueba"
        val extension = "mp3"

        val service = SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { TOKEN_JWT }

        val result = service.uploadSong(
            songName = songName,
            songGenreId = songGenreId,
            description = description,
            extension = extension,
            fileData = songData
        )
        println("----------RESULTADO----------")
        when (result) {
            is GrpcResult.Success -> {
                println("Nice")

            }

            is GrpcResult.GrpcError -> fail("HTTP error: ${result.statusCode} - ${result.message}")
            is GrpcResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is GrpcResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
        assertTrue(result is GrpcResult.Success)

        val uploadResponse = (result as GrpcResult.Success).data
        println("Upload result: ${uploadResponse?.result}, message: ${uploadResponse?.message}")

        assertNotNull(uploadResponse)
        assertTrue(uploadResponse!!.result)
    }

    @Test
    fun uploadSongSimple_realCall_success() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val context = ApplicationProvider.getApplicationContext<Context>()

        val inputStream = context.assets.open("test_song.mp3")
        val songData = inputStream.readBytes()

        val songName = "CancionSimpleUpload"
        val genreId = 2
        val description = "Canción subida con uploadSongSimple"
        val extension = "mp3"

        val service = SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { TOKEN_JWT }


        val result = service.uploadSongSimple(
            songName = songName,
            genreId = genreId,
            description = description,
            extension = extension,
            fileData = songData
        )

        when (result) {
            is GrpcResult.Success -> {
                println("Nice")
            }

            is GrpcResult.GrpcError -> fail("HTTP error: ${result.statusCode} - ${result.message}")
            is GrpcResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is GrpcResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
        assertTrue(result is GrpcResult.Success)

        val uploadResponse = (result as GrpcResult.Success).data
        assertNotNull(uploadResponse)
        assertTrue(uploadResponse!!.result)
        assertTrue(uploadResponse.message.isNotBlank())
    }
}