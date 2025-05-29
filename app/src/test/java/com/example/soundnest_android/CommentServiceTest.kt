package com.example.soundnest_android

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.comment.CreateCommentRequest
import com.example.soundnest_android.restful.services.CommentService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class CommentServiceTest {
    @Before
    fun setup() {
        RestfulRoutes.setBaseUrl("https://localhost:6969/")
    }

    @Test
    fun `create comment should succeed`() = runBlocking {
        val service = CommentService(RestfulRoutes.getBaseUrl())
        val request = CreateCommentRequest(
            songId = 213,
            user = "Pepe",
            message = "Prueba"
        )
        val result = service.postComment(request)

        when (result) {
            is ApiResult.Success -> {
                println("Notification created successfully")
            }

            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
    }

    @Test
    fun `fetch comments by songid should succeed`() = runBlocking {
        val service = CommentService(RestfulRoutes.getBaseUrl())
        val result = service.fetchComments("213")

        when (result) {
            is ApiResult.Success -> {
                Assert.assertNotNull(result.data)
                Assert.assertTrue(result.data?.isNotEmpty() == true)
                println("Notification created successfully")
            }

            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
    }
}