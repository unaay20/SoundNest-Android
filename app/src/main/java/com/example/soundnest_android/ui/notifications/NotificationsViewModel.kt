package com.example.soundnest_android.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<String>?>()

    val notifications: MutableLiveData<List<String>?> = _notifications

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            val notificationsFromApi = fetchNotificationsFromApi()

            withContext(Dispatchers.Main) {
                _notifications.value = notificationsFromApi
            }
        }
    }

    private fun fetchNotificationsFromApi(): List<String> {
        return listOf(
            "👋 ¡Bienvenido! Estas son tus notificaciones.",
            "🔔 Tienes 3 solicitudes de amistad pendientes.",
            "📸 Alguien comentó tu foto."
        )
    }

    fun removeNotification(position: Int) {
        val currentNotifications = _notifications.value?.toMutableList()
        currentNotifications?.removeAt(position)
        _notifications.value = currentNotifications
    }
}
