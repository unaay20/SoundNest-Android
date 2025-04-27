package com.example.soundnest_android.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<String>>()
    val notifications: LiveData<List<String>> = _notifications

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        // TODO: reemplaza esto con llamada a tu repositorio real
        _notifications.value = listOf(
            "👋 ¡Bienvenido! Estas son tus notificaciones.",
            "🔔 Tienes 3 solicitudes de amistad pendientes.",
            "📸 Alguien comentó tu foto."
        )
    }
}
