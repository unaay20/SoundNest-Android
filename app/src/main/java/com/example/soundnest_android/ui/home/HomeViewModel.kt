package com.example.soundnest_android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _navigateToNotifications = MutableLiveData<Boolean>()
    val navigateToNotifications: LiveData<Boolean> = _navigateToNotifications

    fun onNotificationsClicked() {
        _navigateToNotifications.value = true
    }

    fun onNavigated() {
        _navigateToNotifications.value = false
    }
}
