package com.example.soundnest_android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.soundnest_android.grpc.constants.GrpcConstants
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.grpc.http.GrpcResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test


class SongFileGrpcServiceIntegrationTest {
    @Test
    fun uploadSongRealCall() = runBlocking {
        GrpcConstants.setHost("10.0.2.2")
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Precondition: valid file
        val inputStream = context.assets.open("test_song.mp3")
        val songData = inputStream.readBytes()

        val songName = "MiCancionDePrueba"
        val songGenreId = 1
        val description = "Una canción de prueba"
        val extension = "mp3"

        val service = SongFileGrpcService(
            GrpcConstants.getHost(),
            GrpcConstants.getPort()
        ) { "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MiwidXNlcm5hbWUiOiIxIiwiZW1haWwiOiJ6czIyMDEzNjk4QGVzdHVkaWFudGVzLnV2Lm14Iiwicm9sZSI6MiwiaWF0IjoxNzQ2ODQzMjk4LCJleHAiOjE3NDY5MjYwOTh9.h7JUvdPxKKFjG-aoku4Ky26PVdyNHVJepsV1SrQ0ZNQ" }  // Si necesitas token de auth

        val result = service.uploadSong(
            songName = songName,
            songGenreId = songGenreId,
            description = description,
            extension = extension,
            fileData = songData
        )

        println("Resultado upload: $result")

        assertTrue(result is GrpcResult.Success)

        val uploadResponse = (result as GrpcResult.Success).data
        assertNotNull(uploadResponse)
        assertTrue(uploadResponse!!.result)  // Esperamos que el servidor devuelva `result = true`
    }
}