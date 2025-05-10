package com.example.soundnest_android.ui.register

import androidx.lifecycle.*
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.ApiRoutes.BASE_URL
import com.example.soundnest_android.restful.models.user.AdditionalInformation
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.launch

sealed class SendCodeState {
    object Idle    : SendCodeState()
    object Loading : SendCodeState()
    object Success : SendCodeState()
    data class Error(val msg: String) : SendCodeState()
}

sealed class RegisterState {
    object Idle    : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val msg: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val _sendCodeState = MutableLiveData<SendCodeState>(SendCodeState.Idle)
    val sendCodeState: LiveData<SendCodeState> = _sendCodeState

    private val _state = MutableLiveData<RegisterState>(RegisterState.Idle)
    val state: LiveData<RegisterState> = _state

    private val authService = AuthService(BASE_URL)
    private val userService = UserService(BASE_URL)

    fun sendCode(email: String) {
        _sendCodeState.value = SendCodeState.Loading
        viewModelScope.launch {
            when (val r = authService.sendCodeToEmail(email)) {
                is ApiResult.Success       -> _sendCodeState.value = SendCodeState.Success
                is ApiResult.HttpError     -> _sendCodeState.value = SendCodeState.Error("HTTP ${r.code}: ${r.message}")
                is ApiResult.NetworkError  -> _sendCodeState.value = SendCodeState.Error("Red: ${r.exception.message}")
                is ApiResult.UnknownError  -> _sendCodeState.value = SendCodeState.Error("Error: ${r.exception.message}")
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        code: String,
        additionalInformation: AdditionalInformation
    ) {
        _state.value = RegisterState.Loading
        viewModelScope.launch {
            when (val r = userService.createUser(
                username, email, password, code, additionalInformation
            )) {
                is ApiResult.Success       -> _state.value = RegisterState.Success
                is ApiResult.HttpError     -> _state.value = RegisterState.Error("HTTP ${r.code}: ${r.message}")
                is ApiResult.NetworkError  -> _state.value = RegisterState.Error("Red: ${r.exception.message}")
                is ApiResult.UnknownError  -> _state.value = RegisterState.Error("Error: ${r.exception.message}")
            }
        }
    }
}
