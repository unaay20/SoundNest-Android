package com.example.soundnest_android.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class UserProfile(
    val username: String,
    val email: String,
    val role: String,
    val photoUrl: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _profile = MutableLiveData<UserProfile>().apply {
        value = UserProfile(
            username = "NombreUsuario",
            email = "correo@ejemplo.com",
            role = "Escucha",
            photoUrl = null
        )
    }
    val profile: LiveData<UserProfile> = _profile

    private val _editEvent = MutableLiveData<Unit>()
    val editEvent: LiveData<Unit> = _editEvent

    private val _logoutEvent = MutableLiveData<Unit>()
    val logoutEvent: LiveData<Unit> = _logoutEvent

    fun onEditClicked() {
        _editEvent.value = Unit
    }

    fun onLogoutClicked() {
        _logoutEvent.value = Unit
    }

    fun setUserFromPrefs(username: String, email: String, role: String) {
        _profile.value = _profile.value?.copy(username = username)
        _profile.value = _profile.value?.copy(email = email)
        _profile.value = _profile.value?.copy(role = role)
    }
}
