package com.example.soundnest_android.grpc.services

import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.song.SongServiceGrpcKt.SongServiceCoroutineStub
import com.example.soundnest_android.song.UploadResponse
import com.example.soundnest_android.song.UploadSongChunk
import com.example.soundnest_android.song.UploadSongMetadata
import com.example.soundnest_android.song.UploadStreamRequest
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.flow
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
    ): GrpcResult<UploadResponse?> {
        return safeCall {
            val requestFlow = flow {
                val metadata = UploadSongMetadata.newBuilder()
                    .setSongName(songName)
                    .setIdSongGenre(songGenreId)
                    .setDescription(description)
                    .setExtension(extension)
                    .build()
                emit(
                    UploadStreamRequest.newBuilder()
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
                        UploadStreamRequest.newBuilder()
                            .setChunk(chunk)
                            .build()
                    )

                    offset = end
                }

            }
            stub.uploadSongStream(requestFlow)
        }

    }

}