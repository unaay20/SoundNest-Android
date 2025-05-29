package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.playlist.SongIdsRequest
import com.example.soundnest_android.restful.models.song.ExtensionResponse
import com.example.soundnest_android.restful.models.song.GenreResponse
import com.example.soundnest_android.restful.models.song.GetPopularSongResponse
import com.example.soundnest_android.restful.models.song.GetRandomSongResponse
import com.example.soundnest_android.restful.models.song.GetRecentSongResponse
import com.example.soundnest_android.restful.models.song.GetSongDetailResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ISongService {
    @DELETE(RestfulRoutes.SONG_DELETE)
    suspend fun deleteSong(
        @Path("idsong") songId: Int
    ): Response<Unit>

    @POST(RestfulRoutes.SONG_GET_LIST_BY_IDS)
    suspend fun getSongsByIds(
        @Body request: SongIdsRequest
    ): Response<List<GetSongDetailResponse>>

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
        @Query("songName") songName: String? = null,
        @Query("artistName") artistName: String? = null,
        @Query("idGenre") idGenre: Int? = null,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
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