package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.visits.GenrePlayCount
import com.example.soundnest_android.restful.models.visits.SongPlayCount
import com.example.soundnest_android.restful.services.interfaces.IVisitService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider

class VisitService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {

    private val visitApi: IVisitService = retrofit.create(IVisitService::class.java)

    suspend fun incrementVisit(idsong: Int): ApiResult<Unit?> {
        return safeCall { visitApi.incrementVisit(idsong) }
    }

    suspend fun getTopSongsByUser(
        idAppUser: Int,
        limit: Int = 5
    ): ApiResult<List<SongPlayCount>?> {
        return safeCall { visitApi.getTopSongsByUser(idAppUser, limit) }
    }

    suspend fun getTopSongsGlobal(limit: Int = 5): ApiResult<List<SongPlayCount>?> {
        return safeCall { visitApi.getTopSongsGlobal(limit) }
    }

    suspend fun getTopGenresGlobal(limit: Int = 5): ApiResult<List<GenrePlayCount>?> {
        return safeCall { visitApi.getTopGenresGlobal(limit) }
    }
}