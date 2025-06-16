package com.example.soundnest_android.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.launch

sealed class SendCodeState {
    object Idle : SendCodeState()
    object Loading : SendCodeState()
    object Success : SendCodeState()
    data class Error(val msg: String) : SendCodeState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val msg: String) : RegisterState()
}

class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val _sendCodeState = MutableLiveData<SendCodeState>(SendCodeState.Idle)
    val sendCodeState: LiveData<SendCodeState> = _sendCodeState

    private val _state = MutableLiveData<RegisterState>(RegisterState.Idle)
    val state: LiveData<RegisterState> = _state

    private val authService = AuthService(RestfulRoutes.getBaseUrl())
    private val userService =
        UserService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(getApplication()))

    fun sendCode(email: String) {
        _sendCodeState.value = SendCodeState.Loading
        viewModelScope.launch {
            when (val r = authService.sendCodeToEmail(email)) {
                is ApiResult.Success -> _sendCodeState.value = SendCodeState.Success
                is ApiResult.HttpError -> _sendCodeState.value =
                    SendCodeState.Error("HTTP ${r.code}: ${r.message}")

                is ApiResult.NetworkError -> _sendCodeState.value =
                    SendCodeState.Error("Red: ${r.exception.message}")

                is ApiResult.UnknownError -> _sendCodeState.value =
                    SendCodeState.Error("Error: ${r.exception.message}")
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        code: String,
        additionalInformation: String
    ) {
        _state.value = RegisterState.Loading
        viewModelScope.launch {
            when (val r = userService.createUser(
                username, email, password, code, additionalInformation
            )) {
                is ApiResult.Success -> _state.value = RegisterState.Success
                is ApiResult.HttpError -> {
                    val body = r.errorBody ?: ""

                    val errorRes = when {
                        body.contains("Email already exists") -> R.string.error_email_exists
                        body.contains("User name already exists") -> R.string.error_username_exists
                        body.contains("Code not valid") -> R.string.error_invalid_code
                        else -> R.string.error_registration_generic
                    }

                    _state.value =
                        RegisterState.Error(getApplication<Application>().getString(errorRes))
                }


                is ApiResult.NetworkError -> _state.value =
                    RegisterState.Error("Red: ${r.exception.message}")

                is ApiResult.UnknownError -> _state.value =
                    RegisterState.Error("Error: ${r.exception.message}")
            }
        }
    }
}
