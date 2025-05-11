package com.example.soundnest_android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.grpc.http.GrpcResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

const val TOKEN_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MiwidXNlcm5hbWUiOiIxIiwiZW1haWwiOiJ6czIyMDEzNjk4QGVzdHVkaWFudGVzLnV2Lm14Iiwicm9sZSI6MiwiaWF0IjoxNzQ2OTMzNDI3LCJleHAiOjE3NDcwMTYyMjd9.Pz3j6bcJ0H7BoKjVO5A1L3uJ0U1MA2AAsu9Z7nWN1Qk"

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

        println("Resultado upload: $result")

        assertTrue(result is GrpcResult.Success)

        val uploadResponse = (result as GrpcResult.Success).data
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

        println("Resultado uploadSongSimple: $result")

        assertTrue(result is GrpcResult.Success)

        val uploadResponse = (result as GrpcResult.Success).data
        assertNotNull(uploadResponse)
        assertTrue(uploadResponse!!.result)
        assertTrue(uploadResponse.message.isNotBlank())
    }
}