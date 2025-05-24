package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.playlist.SongIdsRequest
import com.example.soundnest_android.restful.models.song.*
import com.example.soundnest_android.restful.services.interfaces.ISongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SongService(
    baseUrl: String,
    tokenProvider: TokenProvider
) : BaseService(baseUrl, tokenProvider) {
    private val api = retrofit.create(ISongService::class.java)

    suspend fun deleteSong(songId: Int): ApiResult<Unit?> =
        safeCall { api.deleteSong(songId) }

    suspend fun getByIds(songIds: List<Int>): ApiResult<List<GetSongDetailResponse>?> {
        val req = SongIdsRequest(songIds)
        return safeCall { api.getSongsByIds(req) }
    }

    suspend fun uploadImage(songId: String, imageFile: File): ApiResult<Unit?> {
        val part = MultipartBody.Part.createFormData(
            "imageFile",
            imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        return safeCall { api.uploadSongImage(songId, part) }
    }

    suspend fun getLatest(userId: Int): ApiResult<GetSongDetailResponse?> =
        safeCall { api.getLatestSongByUser(userId) }

    suspend fun getByUser(userId: Int): ApiResult<List<GetSongDetailResponse>?> =
        safeCall { api.getSongsByUser(userId) }

    suspend fun search(
        songName:   String? = null,
        artistName: String? = null,
        genreId:    Int?    = null,
        limit:      Int     = 10,
        offset:     Int     = 0
    ): ApiResult<List<GetSongDetailResponse>?> =
        safeCall { api.searchSongs(songName, artistName, genreId, limit, offset) }


    suspend fun getRandom(amount: Int): ApiResult<List<GetRandomSongResponse>?> =
        safeCall { api.getRandomSongs(amount) }

    suspend fun getById(songId: Int): ApiResult<GetSongDetailResponse?> =
        safeCall { api.getSongById(songId) }

    suspend fun getGenres(): ApiResult<List<GenreResponse>?> =
        safeCall { api.getGenres() }

    suspend fun getExtensions(): ApiResult<List<ExtensionResponse>?> =
        safeCall { api.getExtensions() }

    suspend fun getPopularByMonth(
        amount: Int, year: Int, month: Int
    ): ApiResult<List<GetPopularSongResponse>?> =
        safeCall { api.getPopularSongsByMonth(amount, year, month) }

    suspend fun getRecent(amount: Int): ApiResult<List<GetRecentSongResponse>?> =
        safeCall { api.getRecentSongs(amount) }
}