package com.example.soundnest_android.restful.services

import com.example.soundnest_android.restful.models.notification.CreateNotificationRequest
import restful.models.notification.NotificationResponse
import com.example.soundnest_android.restful.services.interfaces.INotificationService
import com.example.soundnest_android.restful.utils.ApiResult

class NotificationService(baseUrl: String) : BaseService(baseUrl) {
    private val api = retrofit.create(INotificationService::class.java)

    suspend fun createNotification(request: CreateNotificationRequest): ApiResult<Unit?> {
        return safeCall { api.createNotification(request) }
    }

    suspend fun getNotificationById(id: String): ApiResult<NotificationResponse?> {
        return safeCall { api.getNotificationById(id) }
    }

    suspend fun getNotificationsByUserId(userId: String): ApiResult<List<NotificationResponse>?> {
        return safeCall { api.getNotificationsByUserId(userId) }
    }

    suspend fun deleteNotification(id: String): ApiResult<Unit?> {
        return safeCall { api.deleteNotification(id) }
    }

    suspend fun markNotificationAsRead(id: String): ApiResult<Unit?> {
        return safeCall { api.markNotificationAsRead(id) }
    }
}