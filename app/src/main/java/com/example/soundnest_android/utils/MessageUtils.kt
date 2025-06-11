package com.example.soundnest_android.utils

import android.content.Context
import com.example.soundnest_android.R
import com.example.soundnest_android.restful.utils.ApiResult

fun ApiResult<*>.toDisplayMessage(ctx: Context): String = when (this) {
    is ApiResult.HttpError -> ctx.getString(R.string.error_http, code, message)
    is ApiResult.NetworkError -> ctx.getString(R.string.error_network, exception.message)
    is ApiResult.UnknownError -> ctx.getString(R.string.error_unknown, exception.message)
    else -> ""
}

fun ApiResult<*>.toSimpleError(): String = when (this) {
    is ApiResult.HttpError -> "Error ${code}: ${message}"
    is ApiResult.NetworkError -> "Network error: ${exception.message}"
    is ApiResult.UnknownError -> "Unknown error: ${exception.message}"
    else -> ""
}