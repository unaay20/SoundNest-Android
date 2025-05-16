// SongService.kt
package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.song.GetPopularSongResponse
import com.example.soundnest_android.restful.models.song.GetRecentSongResponse
import com.example.soundnest_android.restful.services.interfaces.ISongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider

class SongService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {

    private val api = retrofit.create(ISongService::class.java)

    suspend fun getPopularByMonth(
        amount: Int,
        year: Int,
        month: Int
    ): ApiResult<List<GetPopularSongResponse>?> {
        return safeCall {
            api.getPopularSongsByMonth(amount, year, month)
        }
    }

    suspend fun getRecent(
        amount: Int
    ): ApiResult<List<GetRecentSongResponse>?> {
        return safeCall {
            api.getRecentSongs(amount)
        }
    }
}
