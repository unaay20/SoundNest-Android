package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.playlist.CreatePlaylistResponse
import com.example.soundnest_android.restful.models.playlist.PlaylistsResponse
import com.example.soundnest_android.restful.services.interfaces.IPlaylistService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PlaylistService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {

    private val api = retrofit.create(IPlaylistService::class.java)

    suspend fun addSongToPlaylist(songId: String, playlistId: String): ApiResult<Unit?> =
        safeCall { api.addSongToPlaylist(songId, playlistId) }

    suspend fun removeSongFromPlaylist(songId: String, playlistId: String): ApiResult<Unit?> =
        safeCall { api.removeSongFromPlaylist(songId, playlistId) }

    suspend fun fetchByUser(userId: String): ApiResult<PlaylistsResponse?> =
        safeCall { api.getPlaylistsByUser(userId) }

    suspend fun deletePlaylist(playlistId: String): ApiResult<Unit?> =
        safeCall { api.deletePlaylist(playlistId) }

    suspend fun createPlaylist(
        name: String,
        description: String?,
        imageFile: File
    ): ApiResult<CreatePlaylistResponse?> {
        val nameBody = name.toRequestBody("text/plain".toMediaType())
        val descBody = description?.toRequestBody("text/plain".toMediaType())

        val imagePart = MultipartBody.Part.createFormData(
            "image",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        return safeCall {
            api.createPlaylist(
                image = imagePart,
                playlistName = nameBody,
                description = descBody
            )
        }
    }
}