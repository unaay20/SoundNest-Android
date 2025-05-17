package com.example.soundnest_android.restful.models.song

import com.squareup.moshi.Json

data class ExtensionResponse(
    @Json(name = "idSongExtension") val idSongExtension: Int,
    @Json(name = "extensionName") val extensionName: String
)