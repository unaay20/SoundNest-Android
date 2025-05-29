package com.example.soundnest_android

import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class AuthServiceTest {
    @Before
    fun setup() {
        RestfulRoutes.setBaseUrl("https://localhost:6969/")
    }

    @Test
    fun `login with username 1 and password 1 should succeed`() = runBlocking {
        val baseUrl = RestfulRoutes.getBaseUrl()
        val loginClient = AuthService(baseUrl)
        val result = loginClient.login("1", "1")

        when (result) {
            is ApiResult.Success -> {
                assertNotNull(result.data)
            }

            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }

    }

    @Test
    fun `send email code to email should succeed`() = runBlocking {
        val baseUrl = RestfulRoutes.getBaseUrl()
        val loginClient = AuthService(baseUrl)
        val result = loginClient.sendCodeToEmail("zs22013698@estudiantes.uv.mx")
        when (result) {
            is ApiResult.Success -> {
                println("Nice")
            }

            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }

    }
}