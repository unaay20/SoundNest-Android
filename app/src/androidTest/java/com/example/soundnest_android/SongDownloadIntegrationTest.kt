package com.example.soundnest_android

import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream

class SongDownloadIntegrationTest {
    private val testSongId = 3

    @Test
    fun downloadSongSimple_shouldReturnMetadataAndData() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val service = SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { null }
        println("Enviando songId = $testSongId")
        val result = service.downloadSongSimple(testSongId)

        println("Resultado simple: $result")
        assertTrue(result is GrpcResult.Success)

        val (metadata, bytes) = (result as GrpcResult.Success).data!!
        assertNotNull(metadata)
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
        assertTrue(metadata.songName.isNotBlank())
    }

    @Test
    fun downloadSongStreamTo_shouldCollectDataAndMetadata() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        val service = SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { null }

        val outputStream = ByteArrayOutputStream()
        val result = service.downloadSongStreamTo(testSongId, outputStream)

        println("Resultado stream: $result")
        assertTrue(result is GrpcResult.Success)

        val metadata = (result as GrpcResult.Success).data
        val data = outputStream.toByteArray()

        assertNotNull(metadata)
        assertNotNull(data)
        assertTrue(data.isNotEmpty())
        assertTrue(metadata!!.songName.isNotBlank())
    }
}