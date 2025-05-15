package com.example.soundnest_android.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel : ViewModel() {

    private val _profile = MutableLiveData<UserProfile>().apply {
        value = UserProfile(
            username = "NombreUsuario",
            email    = "correo@ejemplo.com",
            role     = "Escucha",
            photoUrl = ""
        )
    }
    val profile: LiveData<UserProfile> = _profile

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    fun onSaveClicked(newUsername: String, newEmail: String) {
        _profile.value = _profile.value?.copy(
            username = newUsername,
            email = newEmail
        )
        _saveResult.value = true
    }
    fun onLoadInitial(username: String, email: String) {
        _profile.value = _profile.value?.copy(
            username = username,
            email    = email
        )
    }
}