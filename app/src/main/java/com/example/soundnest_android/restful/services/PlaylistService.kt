package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.playlist.PlaylistResponse
import com.example.soundnest_android.restful.models.playlist.CreatePlaylistResponse
import com.example.soundnest_android.restful.services.interfaces.IPlaylistService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PlaylistService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {
    private val api = retrofit.create(IPlaylistService::class.java)

    suspend fun fetchByUser(userId: String): ApiResult<List<PlaylistResponse>?> =
        safeCall { api.getPlaylistsByUser(userId) }

    suspend fun addSong(songId: String, playlistId: String): ApiResult<Unit?> =
        safeCall { api.addSongToPlaylist(songId, playlistId) }

    suspend fun removeSong(songId: String, playlistId: String): ApiResult<Unit?> =
        safeCall { api.removeSongFromPlaylist(songId, playlistId) }

    suspend fun deletePlaylist(playlistId: String): ApiResult<Unit?> =
        safeCall { api.deletePlaylist(playlistId) }

    suspend fun createPlaylist(name: String, imageFile: File): ApiResult<CreatePlaylistResponse?> {
        val imagePart = MultipartBody.Part.createFormData(
            "imageFile",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        return safeCall { api.createPlaylist(name, imagePart) }
    }
}