package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.playlist.CreatePlaylistResponse
import com.example.soundnest_android.restful.models.playlist.EditPlaylistRequest
import com.example.soundnest_android.restful.models.playlist.EditPlaylistResponse
import com.example.soundnest_android.restful.models.playlist.PlaylistsResponse
import com.example.soundnest_android.restful.services.interfaces.IPlaylistService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import okhttp3.MultipartBody
import okhttp3.RequestBody

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
        image: MultipartBody.Part,
        playlistName: RequestBody,
        description: RequestBody?
    ): ApiResult<CreatePlaylistResponse?> = safeCall {
        api.createPlaylist(
            image = image,
            playlistName = playlistName,
            description = description
        )
    }

    suspend fun editPlaylist(
        idPlaylist: String,
        newName: String,
        newDescription: String?
    ): ApiResult<EditPlaylistResponse?> {
        val req = EditPlaylistRequest(
            playlistName = newName,
            description = newDescription
        )
        return safeCall { api.editPlaylist(idPlaylist, req) }
    }
}