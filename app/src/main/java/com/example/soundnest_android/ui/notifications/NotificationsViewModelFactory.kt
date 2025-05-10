package com.example.soundnest_android.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.restful.services.NotificationService

class NotificationsViewModelFactory(
    private val notificationService: NotificationService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(notificationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
