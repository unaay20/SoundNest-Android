package com.example.soundnest_android.utils

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object UriToFileUtil {
    suspend fun getFileFromUri(context: Context, uri: Uri): File? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Glide.with(context)
                    .downloadOnly()
                    .load(uri)
                    .submit()
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
