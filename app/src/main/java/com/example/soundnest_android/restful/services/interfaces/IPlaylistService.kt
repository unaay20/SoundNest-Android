package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.playlist.CreatePlaylistResponse
import com.example.soundnest_android.restful.models.playlist.EditPlaylistRequest
import com.example.soundnest_android.restful.models.playlist.PlaylistsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface IPlaylistService {
    @GET(RestfulRoutes.PLAYLIST_GET_BY_USERID)
    suspend fun getPlaylistsByUser(@Path("iduser") userId: String): Response<PlaylistsResponse>

    @PATCH(RestfulRoutes.PLAYLIST_PATCH_ADD_SONG)
    suspend fun addSongToPlaylist(
        @Path("idsong") songId: String,
        @Path("idPlaylist") playlistId: String
    ): Response<Unit>

    @PATCH(RestfulRoutes.PLAYLIST_PATCH_REMOVE_SONG)
    suspend fun removeSongFromPlaylist(
        @Path("idsong") songId: String,
        @Path("idPlaylist") playlistId: String
    ): Response<Unit>

    @DELETE(RestfulRoutes.PLAYLIST_DELETE)
    suspend fun deletePlaylist(@Path("idPlaylist") playlistId: String): Response<Unit>

    @PUT(RestfulRoutes.PLAYLIST_PUT_NEW_PLAYLIST)
    @Multipart
    suspend fun createPlaylist(
        @Part image: MultipartBody.Part,
        @Part("playlistName") playlistName: RequestBody,
        @Part("description") description: RequestBody?
    ): Response<CreatePlaylistResponse>

    @PATCH(RestfulRoutes.PLAYLIST_PATCH_EDIT)
    suspend fun editPlaylist(
        @Path("idPlaylist") idPlaylist: String,
        @Body request: EditPlaylistRequest
    ): Response<CreatePlaylistResponse>
}