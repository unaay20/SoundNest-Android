package com.example.soundnest_android.ui.change_password

import androidx.lifecycle.*
import com.example.soundnest_android.restful.constants.RestfulRoutes
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

sealed class ChangePasswordState {
    object Idle    : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val msg: String) : ChangePasswordState()
}

class ChangePasswordViewModel : ViewModel() {
    private val _sendCodeState = MutableLiveData<SendCodeState>(SendCodeState.Idle)
    val sendCodeState: LiveData<SendCodeState> = _sendCodeState

    private val _changeState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changeState: LiveData<ChangePasswordState> = _changeState

    private val authService = AuthService(RestfulRoutes.getBaseUrl())
    private val userService = UserService(RestfulRoutes.getBaseUrl())

    fun sendCode(email: String) {
        _sendCodeState.value = SendCodeState.Loading
        viewModelScope.launch {
            when (val r = authService.sendCodeToEmail(email)) {
                is ApiResult.Success -> _sendCodeState.value = SendCodeState.Success
                is ApiResult.HttpError -> _sendCodeState.value = SendCodeState.Error("HTTP ${r.code}: ${r.message}")
                is ApiResult.NetworkError -> _sendCodeState.value = SendCodeState.Error("Red: ${r.exception.message}")
                is ApiResult.UnknownError -> _sendCodeState.value = SendCodeState.Error("Error: ${r.exception.message}")
            }
        }
    }

    fun changePassword(code: String, newPassword: String) {
        _changeState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            val result = userService.editUserPassword(code, newPassword)

            when (result) {
                is ApiResult.Success -> {
                    _changeState.value = ChangePasswordState.Success
                }
                is ApiResult.HttpError -> {
                    _changeState.value = ChangePasswordState.Error("HTTP ${result.code}: ${result.message}")
                }
                is ApiResult.NetworkError -> {
                    _changeState.value = ChangePasswordState.Error("Red: ${result.exception.message}")
                }
                is ApiResult.UnknownError -> {
                    _changeState.value = ChangePasswordState.Error("Error: ${result.exception.message}")
                }
            }
        }
    }
}
