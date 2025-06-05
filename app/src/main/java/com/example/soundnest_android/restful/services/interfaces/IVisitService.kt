package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.models.visits.GenrePlayCount
import com.example.soundnest_android.restful.models.visits.SongPlayCount
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IVisitService {
    @GET("api/visit/user/{idAppUser}/top-songs")
    suspend fun getTopSongsByUser(
        @Path("idAppUser") idAppUser: Int,
        @Query("limit") limit: Int = 10
    ): Response<List<SongPlayCount>>

    @GET("api/visit/global/top-songs")
    suspend fun getTopSongsGlobal(
        @Query("limit") limit: Int = 10
    ): Response<List<SongPlayCount>>

    @GET("api/visit/global/top-genres")
    suspend fun getTopGenresGlobal(
        @Query("limit") limit: Int = 10
    ): Response<List<GenrePlayCount>>
}