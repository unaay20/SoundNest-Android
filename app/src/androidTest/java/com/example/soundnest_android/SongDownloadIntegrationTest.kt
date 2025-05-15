package com.example.soundnest_android

import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.song.DownloadSongData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream

class SongDownloadIntegrationTest {
    private val testSongId = 1

    @Test
    fun downloadSongSimple_shouldReturnMetadataAndData() = runBlocking {
        GrpcRoutes.setHost("10.0.2.2")
        GrpcRoutes.setPort(50051)
        val service = SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { null }
        println("Enviando songId = $testSongId")
        var result: GrpcResult<Pair<DownloadSongData, ByteArray>?> = service.downloadSongSimple(testSongId)

        println("Resultado simple: $result")
        when (result) {
            is GrpcResult.Success -> {
                println("Nice")
            }
            is GrpcResult.GrpcError -> fail("HTTP error: ${result.statusCode} - ${result.message}")
            is GrpcResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is GrpcResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
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