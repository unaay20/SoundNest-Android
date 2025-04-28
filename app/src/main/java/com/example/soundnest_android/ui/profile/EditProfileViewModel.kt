package com.example.soundnest_android.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel : ViewModel() {

    // Data class del perfil reutilizada
    private val _profile = MutableLiveData<UserProfile>().apply {
        // Pre-cargar con datos actuales (mock o repo real)
        value = UserProfile(
            username = "NombreUsuario",
            email    = "correo@ejemplo.com",
            role     = "Escucha",
            photoUrl = null
        )
    }
    val profile: LiveData<UserProfile> = _profile

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    /** Llamado al presionar "Guardar" */
    fun onSaveClicked(newUsername: String, newEmail: String) {
        // Aquí llamarías a tu repositorio para guardar cambios
        // Simulamos un guardado exitoso:
        _profile.value = _profile.value?.copy(
            username = newUsername,
            email = newEmail
        )
        _saveResult.value = true
    }
}