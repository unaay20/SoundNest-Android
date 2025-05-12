package com.example.soundnest_android

import com.example.soundnest_android.restful.constants.RestfulRoutes
import kotlinx.coroutines.runBlocking

import com.example.soundnest_android.restful.models.notification.CreateNotificationRequest
import com.example.soundnest_android.restful.services.NotificationService
import com.example.soundnest_android.restful.utils.ApiResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.example.soundnest_android.restful.models.notification.Relevance

class NotificationServiceTest {
    @Before
    fun setup() {
        RestfulRoutes.setBaseUrl("https://localhost:6969/")
    }
    @Test
    fun `create notification should succeed`() = runBlocking {
        val service = NotificationService(RestfulRoutes.getBaseUrl())
        val request = CreateNotificationRequest(
            sender = "Meee",
            userId = 2,
            user = "Pepe",
            notification = "Test Notification",
            relevance = Relevance.low
        )
        val result = service.createNotification(request)

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
    fun `get notification by id should return notification`() = runBlocking {
        val service = NotificationService(RestfulRoutes.getBaseUrl())

        val notificationId: String? = when (val resultById = service.getNotificationsByUserId("1")) {
            is ApiResult.Success -> resultById.data?.firstOrNull()?.id
            is ApiResult.HttpError -> throw Exception("HTTP error: ${resultById.code} - ${resultById.message}")
            is ApiResult.NetworkError -> throw resultById.exception
            is ApiResult.UnknownError -> throw resultById.exception
        }

        val result = service.getNotificationById(notificationId ?: throw Exception("No notification ID found"))

        when (result) {
            is ApiResult.Success -> {
                assertNotNull(result.data?.notification, "Notifications list should not be null")
                println("Received notifications: ${result.data}")
            }
            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
    }

    @Test
    fun `get notifications by user id should return list`() = runBlocking {
        val service = NotificationService(RestfulRoutes.getBaseUrl())
        val userId = "1"

        val result = service.getNotificationsByUserId(userId)

        when (result) {
            is ApiResult.Success -> {
                val notifications = result.data
                assertNotNull(notifications?.firstOrNull().toString(), "Notifications list should not be null")
                println("Received notifications: $notifications")
            }
            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
    }

    @Test
    fun `delete notification should succeed`() = runBlocking {
        val service = NotificationService(RestfulRoutes.getBaseUrl())
        val userIdForDelete = 200
        service.createNotification(
            CreateNotificationRequest(
                sender = "Meee",
                userId = userIdForDelete,
                user = "Pepe",
                notification = "Test Notification",
                relevance = Relevance.low
            )
        )
        val notificationId: String? = when (val resultById = service.getNotificationsByUserId(userIdForDelete.toString())) {
            is ApiResult.Success -> resultById.data?.firstOrNull()?.id
            is ApiResult.HttpError -> throw Exception("HTTP error: ${resultById.code} - ${resultById.message}")
            is ApiResult.NetworkError -> throw resultById.exception
            is ApiResult.UnknownError -> throw resultById.exception
        }


        val result = service.getNotificationById(notificationId ?: throw Exception("No notification ID found"))

        when (result) {
            is ApiResult.Success -> {
                println("Notification deleted successfully")
            }
            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
    }

    @Test
    fun `mark notification as read should succeed`() = runBlocking {
        val service = NotificationService(RestfulRoutes.getBaseUrl())
        val notificationId = "681640001606df47613187f5"

        val result = service.markNotificationAsRead(notificationId)

        when (result) {
            is ApiResult.Success -> {
                println("Notification marked as read successfully")
            }
            is ApiResult.HttpError -> fail("HTTP error: ${result.code} - ${result.message}")
            is ApiResult.NetworkError -> fail("Network error: ${result.exception.message}")
            is ApiResult.UnknownError -> fail("Unknown error: ${result.exception.message}")
        }
    }
}