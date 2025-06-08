package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.visits.GenrePlayCount
import com.example.soundnest_android.restful.models.visits.SongPlayCount
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IVisitService {
    @POST(RestfulRoutes.VISUALIZATION_INCREMENT)
    suspend fun incrementVisit(@Path("idsong") idSong: Int): Response<Unit>

    @GET(RestfulRoutes.VISIT_TOP_SONGS_USER)
    suspend fun getTopSongsByUser(
        @Path("idAppUser") idAppUser: Int,
        @Query("limit") limit: Int = 10
    ): Response<List<SongPlayCount>>

    @GET(RestfulRoutes.VISIT_TOP_SONGS_GLOBAL)
    suspend fun getTopSongsGlobal(
        @Query("limit") limit: Int = 10
    ): Response<List<SongPlayCount>>

    @GET(RestfulRoutes.VISIT_TOP_GENRES_GLOBAL)
    suspend fun getTopGenresGlobal(
        @Query("limit") limit: Int = 10
    ): Response<List<GenrePlayCount>>
}