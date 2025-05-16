package com.example.soundnest_android.restful.services.interfaces

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.song.GetPopularSongResponse
import com.example.soundnest_android.restful.models.song.GetRecentSongResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ISongService {
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