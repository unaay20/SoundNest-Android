package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.playlist.PlaylistResponse
import com.example.soundnest_android.restful.models.playlist.CreatePlaylistResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface IPlaylistService {
    @GET(RestfulRoutes.PLAYLIST_GET_BY_USERID)
    suspend fun getPlaylistsByUser(@Path("iduser") userId: String): Response<List<PlaylistResponse>>

    @PATCH(RestfulRoutes.PLAYLIST_PATCH_ADD_SONG)
    suspend fun addSongToPlaylist(@Path("idsong") songId: String, @Path("idPlaylist") playlistId: String): Response<Unit>

    @PATCH(RestfulRoutes.PLAYLIST_PATCH_REMOVE_SONG)
    suspend fun removeSongFromPlaylist(@Path("idsong") songId: String, @Path("idPlaylist") playlistId: String): Response<Unit>

    @DELETE(RestfulRoutes.PLAYLIST_DELETE)
    suspend fun deletePlaylist(@Path("idPlaylist") playlistId: String): Response<Unit>

    @PUT(RestfulRoutes.PLAYLIST_PUT_NEW_PLAYLIST)
    @Multipart
    suspend fun createPlaylist(@Part("name") name: String,@Part imageFile: MultipartBody.Part): Response<CreatePlaylistResponse>
}