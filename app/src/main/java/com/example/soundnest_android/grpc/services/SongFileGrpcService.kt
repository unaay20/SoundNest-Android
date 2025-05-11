package com.example.soundnest_android.grpc.services

import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.song.DownloadSongData
import com.example.soundnest_android.song.DownloadSongMetadata
import com.example.soundnest_android.song.DownloadSongRequest
import com.example.soundnest_android.song.DownloadStreamResponse
import com.example.soundnest_android.song.Song
import com.example.soundnest_android.song.SongServiceGrpcKt.SongServiceCoroutineStub
import com.example.soundnest_android.song.UploadSongChunk
import com.example.soundnest_android.song.UploadSongMetadata
import com.example.soundnest_android.song.UploadSongRequest
import com.example.soundnest_android.song.UploadSongResponse
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class SongFileGrpcService(
    host: String,
    port: Int, tokenProvider: () -> String?
) : BaseGrpcService(host, port, tokenProvider) {
        private val stub = SongServiceCoroutineStub(channel)

        suspend fun uploadSong(    songName: String,
                                   songGenreId: Int,
                                   description: String,
                                   extension: String,
                                   fileData: ByteArray,
                                   chunkSize: Int = 1024 * 64 // 64KB per chunk
        ): GrpcResult<UploadSongResponse?> {
            return safeCall {
                val requestFlow = flow {
                    val metadata = UploadSongMetadata.newBuilder()
                        .setSongName(songName)
                        .setIdSongGenre(songGenreId)
                        .setDescription(description)
                        .setExtension(extension)
                        .build()
                    emit(
                        UploadSongRequest.newBuilder()
                            .setMetadata(metadata)
                            .build()
                    )
                    var offset = 0
                    while (offset < fileData.size) {
                        val end = minOf(offset + chunkSize, fileData.size)
                        val chunk = UploadSongChunk.newBuilder()
                            .setChunkData(ByteString.copyFrom(fileData, offset, end - offset))
                            .build()

                        emit(
                            UploadSongRequest.newBuilder()
                                .setChunk(chunk)
                                .build()
                        )

                        offset = end
                    }

                }
            stub.uploadSongStream(requestFlow)
        }
    }
    suspend fun uploadSongSimple(
        songName: String,
        genreId: Int,
        description: String,
        extension: String,
        fileData: ByteArray
    ): GrpcResult<UploadSongResponse?> {
        return safeCall {
            val request = Song.newBuilder()
                .setSongName(songName)
                .setFile(ByteString.copyFrom(fileData))
                .setIdSongGenre(genreId)
                .setDescription(description)
                .setExtension(extension)
                .build()

            stub.uploadSong(request)
        }
    }
    suspend fun downloadSongSimple(songId: Int): GrpcResult<Pair<DownloadSongData, ByteArray>?> {
        println("Valor que recibo como songId: $songId")
        return safeCall {
            val request = DownloadSongRequest.newBuilder()
                .setIdSong(songId)
                .build()

            println("Request generado: $request")
            val response = stub.downloadSong(request)

            val songBytes = response.file.toByteArray()

            // Retorna tanto la metadata como el archivo
            Pair(response, songBytes)
        }
    }

    suspend fun downloadSongStreamTo(
        songId: Int,
        outputStream: OutputStream
    ): GrpcResult<DownloadSongMetadata?> {
        return safeCall {
            val request = DownloadSongRequest.newBuilder()
                .setIdSong(songId)
                .build()

            val responseFlow: Flow<DownloadStreamResponse> = stub.downloadSongStream(request)

            var metadata: DownloadSongMetadata? = null

            responseFlow.collect { response ->
                when (response.payloadCase) {
                    DownloadStreamResponse.PayloadCase.METADATA -> {
                        metadata = response.metadata
                    }
                    DownloadStreamResponse.PayloadCase.CHUNK -> {
                        val chunk = response.chunk.chunkData
                        outputStream.write(chunk.toByteArray())
                    }
                    else -> {}
                }
            }

            outputStream.flush()
            metadata
        }
    }

}