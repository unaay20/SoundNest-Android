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

    // Simulamos la carga del perfil; en producción vendría de un repositorio o SharedPreferences
    private val _profile = MutableLiveData<UserProfile>().apply {
        value = UserProfile(
            username = "NombreUsuario",
            email    = "correo@ejemplo.com",
            role     = "Moderador",
            photoUrl = null  // si tienes URL remota ponla aquí
        )
    }
    val profile: LiveData<UserProfile> = _profile

    /** Llamar cuando el usuario pulse “Editar perfil” */
    private val _editEvent = MutableLiveData<Unit>()
    val editEvent: LiveData<Unit> = _editEvent
    fun onEditClicked() {
        _editEvent.value = Unit
    }

    /** Llamar cuando el usuario pulse “Cerrar sesión” */
    private val _logoutEvent = MutableLiveData<Unit>()
    val logoutEvent: LiveData<Unit> = _logoutEvent
    fun onLogoutClicked() {
        _logoutEvent.value = Unit
    }
}
