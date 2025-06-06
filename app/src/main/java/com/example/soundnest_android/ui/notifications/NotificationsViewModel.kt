package com.example.soundnest_android.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.restful.models.notification.NotificationResponse
import com.example.soundnest_android.restful.services.NotificationService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsViewModel(private val notificationService: NotificationService) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationResponse>>()
    val notifications: LiveData<List<NotificationResponse>> = _notifications

    fun loadNotifications(userId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (userId != null) {
                val result = fetchNotificationsFromApi(userId.toString())

                withContext(Dispatchers.Main) {
                    handleApiResult(result)
                }
            }
        }
    }

    private fun handleApiResult(result: ApiResult<List<NotificationResponse>?>) {
        when (result) {
            is ApiResult.Success -> {
                _notifications.value = result.data ?: emptyList()
            }

            is ApiResult.HttpError -> {
                Log.e(Constants.NOTIFICATIONS_ACTIVITY, "HTTP error: ${result.message}")
            }

            is ApiResult.NetworkError -> {
                Log.e(Constants.NOTIFICATIONS_ACTIVITY, "Network error: ${result.exception}")
            }

            is ApiResult.UnknownError -> {
                Log.e(Constants.NOTIFICATIONS_ACTIVITY, "Unknown error: ${result.exception}")
            }
        }
    }

    private suspend fun fetchNotificationsFromApi(userId: String): ApiResult<List<NotificationResponse>?> {
        return notificationService.getNotificationsByUserId(userId)
    }

    fun removeNotification(position: Int) {
        val currentNotifications = _notifications.value?.toMutableList() ?: mutableListOf()
        currentNotifications.removeAt(position)
        _notifications.value = currentNotifications
    }
}
