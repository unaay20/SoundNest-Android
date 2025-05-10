package com.example.soundnest_android.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.restful.models.auth.LoginResponse
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.ApiRoutes.BASE_URL
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse) : LoginState()
    data class Error(val msg: String)           : LoginState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    private val tokenProvider = ApiService.tokenProvider
    private val authService    = AuthService(BASE_URL)

    fun login(username: String, password: String) {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            when (val result = authService.login(username, password)) {
                is ApiResult.Success -> {
                    result.data?.let {
                        tokenProvider.saveToken(it.token)
                        _state.value = LoginState.Success(it)
                    } ?: run {
                        _state.value = LoginState.Error("Respuesta vacía del servidor")
                    }
                }
                is ApiResult.HttpError    ->
                    _state.value = LoginState.Error("HTTP ${result.code}: ${result.message}")
                is ApiResult.NetworkError ->
                    _state.value = LoginState.Error("Red: ${result.exception.message}")
                is ApiResult.UnknownError ->
                    _state.value = LoginState.Error("Desconocido: ${result.exception.message}")
            }
        }
    }
}
