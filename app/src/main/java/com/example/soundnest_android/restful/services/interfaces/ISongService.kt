package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.song.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ISongService {
    @DELETE(RestfulRoutes.SONG_DELETE)
    suspend fun deleteSong(
        @Path("idsong") songId: Int
    ): Response<Unit>

    @PATCH(RestfulRoutes.SONG_PATCH_SONG_IMAGE)
    @Multipart
    suspend fun uploadSongImage(
        @Path("idsong") songId: String,
        @Part imageFile: MultipartBody.Part
    ): Response<Unit>

    @GET(RestfulRoutes.SONG_GET_LATEST_SONG)
    suspend fun getLatestSongByUser(
        @Path("idAppUser") userId: Int
    ): Response<GetSongDetailResponse>

    @GET(RestfulRoutes.SONG_GET_SONGS_BY_USERID)
    suspend fun getSongsByUser(
        @Path("idAppUser") userId: Int
    ): Response<List<GetSongDetailResponse>>

    @GET(RestfulRoutes.SONG_GET_SONG_SEARCH_FILTERS)
    suspend fun searchSongs(
        @Query("songName") songName: String?,
        @Query("artistName") artistName: String?,
        @Query("idGenre") genreId: Int?,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Response<List<GetSongDetailResponse>>

    @GET(RestfulRoutes.SONG_GET_RANDOM_SONGS)
    suspend fun getRandomSongs(
        @Path("amount") amount: Int
    ): Response<List<GetRandomSongResponse>>

    @GET(RestfulRoutes.SONG_GET_BY_ID)
    suspend fun getSongById(
        @Path("idsong") songId: Int
    ): Response<GetSongDetailResponse>

    @GET(RestfulRoutes.SONG_GET_GENRES)
    suspend fun getGenres(): Response<List<GenreResponse>>

    @GET(RestfulRoutes.SONG_GET_EXTENSIONS)
    suspend fun getExtensions(): Response<List<ExtensionResponse>>

    @GET(RestfulRoutes.SONG_GET_POPULAR_SONGS)
    suspend fun getPopularSongsByMonth(
        @Path("amount") amount: Int,
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<List<GetPopularSongResponse>>

    @GET(RestfulRoutes.SONG_GET_RECENT_SONGS)
    suspend fun getRecentSongs(
        @Path("amount") amount: Int
    ): Response<List<GetRecentSongResponse>>
}