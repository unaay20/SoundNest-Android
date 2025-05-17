package com.example.soundnest_android.ui.edit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.services.UserService

class EditProfileViewModelFactory(
    private val userService: UserService,
    private val tokenProvider: SharedPrefsTokenProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(userService, tokenProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}