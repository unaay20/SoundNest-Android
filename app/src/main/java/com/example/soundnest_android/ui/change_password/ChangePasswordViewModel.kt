package com.example.soundnest_android.ui.change_password

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.utils.toDisplayMessage
import kotlinx.coroutines.launch

sealed class SendCodeState {
    object Idle : SendCodeState()
    object Loading : SendCodeState()
    object Success : SendCodeState()
    data class Error(val msg: String) : SendCodeState()
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val msg: String) : ChangePasswordState()
}

class ChangePasswordViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val _sendCodeState = MutableLiveData<SendCodeState>(SendCodeState.Idle)
    val sendCodeState: LiveData<SendCodeState> = _sendCodeState

    private val _changeState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changeState: LiveData<ChangePasswordState> = _changeState

    private val authService = AuthService(RestfulRoutes.getBaseUrl())
    private val userService =
        UserService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(getApplication()))

    fun sendCode(email: String) {
        _sendCodeState.value = SendCodeState.Loading
        viewModelScope.launch {
            when (val result = authService.sendCodeToEmail(email)) {
                is ApiResult.Success -> _sendCodeState.value = SendCodeState.Success
                else -> _changeState.value =
                    ChangePasswordState.Error(
                        result.toDisplayMessage(getApplication())
                    )
            }
        }
    }

    fun changePassword(email: String, code: String, newPassword: String) {
        _changeState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            when (val result = userService.editUserPassword(email, code, newPassword)) {
                is ApiResult.Success -> {
                    _changeState.value = ChangePasswordState.Success
                }

                else -> _changeState.value =
                    ChangePasswordState.Error(
                        result.toDisplayMessage(getApplication())
                    )
            }
        }
    }
}
